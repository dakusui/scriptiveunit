package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.exceptions.SyntaxException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Character;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.mergeFailed;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Character.*;
import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.lang.String.format;
import static java.math.MathContext.DECIMAL128;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public enum Utils {
  ;

  public static Tuple filterSingleLevelFactorsOut(Tuple tuple, List<Factor> factors) {
    Tuple.Builder b = new Tuple.Builder();
    factors.stream()
        .filter(each -> each.levels.size() > 1)
        .filter(each -> tuple.containsKey(each.name))
        .forEach(each -> b.put(each.name, tuple.get(each.name)));
    return b.build();
  }

  public static void performActionWithLogging(Action action) {
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter());
    }
  }

  public static ObjectNode deepMerge(ObjectNode source, ObjectNode target) {
    requireNonNull(source);
    requireNonNull(target);
    for (String key : (Iterable<String>) source::getFieldNames) {
      JsonNode sourceValue = source.get(key);
      if (!target.has(key)) {
        // new value for "key":
        target.put(key, sourceValue);
      } else {
        // existing value for "key" - recursively deep merge:
        if (sourceValue.isObject()) {
          ObjectNode sourceObject = (ObjectNode) sourceValue;
          JsonNode targetValue = target.get(key);
          check(targetValue.isObject(), () -> mergeFailed(source, target, key));
          deepMerge(sourceObject, (ObjectNode) targetValue);
        } else {
          target.put(key, sourceValue);
        }
      }
    }
    return target;
  }

  public static String toALL_CAPS(String inputString) {
    StringBuilder b = new StringBuilder();
    boolean wasPreviousUpper = true;
    for (Character each : inputString.toCharArray()) {
      boolean isUpper = isUpperCase(each);
      if (isUpper) {
        if (!wasPreviousUpper)
          b.append("_");
        b.append(each);
      } else {
        b.append(each.toString().toUpperCase());
      }
      wasPreviousUpper = isUpper;
    }
    return b.toString();
  }

  public static String toCamelCase(String inputString) {
    StringBuilder b = new StringBuilder();
    boolean wasPreviousUnderscore = false;
    for (Character each : inputString.toCharArray()) {
      boolean isUnderscore = each.equals('_');
      if (!isUnderscore) {
        if (wasPreviousUnderscore) {
          b.append(toUpperCase(each));
        } else {
          b.append(toLowerCase(each));
        }
      }
      wasPreviousUnderscore = isUnderscore;
    }
    return b.toString();
  }

  public static <T> Constructor<T> getConstructor(Class<? extends T> clazz) {
    Constructor[] constructors = clazz.getConstructors();
    checkState(
        constructors.length == 1,
        "There must be 1 and only 1 public constructor in order to use '%s' as a JCUnit plug-in(%s found). Also please make sure the class is public and static.",
        clazz,
        constructors.length
    );
    //noinspection unchecked
    return (Constructor<T>) constructors[0];
  }

  public static <E extends ScriptiveUnitException> void check(boolean cond, Supplier<E> thrower) {
    if (!cond)
      throw thrower.get();
  }

  public static <E extends ScriptiveUnitException, V> V check(V target, Predicate<? super V> predicate, Supplier<? extends E> thrower) {
    if (!requireNonNull(predicate).test(target))
      throw thrower.get();
    return target;
  }

  public static BigDecimal toBigDecimal(Number number) {
    if (number instanceof BigDecimal)
      return BigDecimal.class.cast(number);
    return new BigDecimal(number.toString(), DECIMAL128);
  }

  public static Object toBigDecimalIfPossible(Object object) {
    if (object instanceof Number) {
      return toBigDecimal(Number.class.cast(object));
    }
    return object;
  }

  // safe because both Long.class and long.class are of type Class<Long>
  @SuppressWarnings("unchecked")
  public static <T> Class<T> wrap(Class<T> c) {
    return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
  }

  private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS
      = new ImmutableMap.Builder<Class<?>, Class<?>>()
      .put(boolean.class, Boolean.class)
      .put(byte.class, Byte.class)
      .put(char.class, Character.class)
      .put(double.class, Double.class)
      .put(float.class, Float.class)
      .put(int.class, Integer.class)
      .put(long.class, Long.class)
      .put(short.class, Short.class)
      .put(void.class, Void.class)
      .build();


  public static boolean isCompatible(Object input, Class<?> to) {
    requireNonNull(to);
    if (to.isPrimitive()) {
      requireNonNull(input);
      return Utils.wrap(to).isAssignableFrom(input.getClass());
    }
    //noinspection SimplifiableIfStatement
    if (input == null)
      return true;
    return to.isAssignableFrom(input.getClass());
  }

  public static <T> T convert(Object input, Class<T> to) {
    requireNonNull(input);
    requireNonNull(to);
    for (Converter each : TYPE_CONVERTERS) {
      //noinspection unchecked
      if (each.supports(input, to))
        //noinspection unchecked
        return (T) each.apply(input);
    }
    throw SyntaxException.typeMismatch(to, input);
  }

  public static <T> Object convertIfNecessary(Object input, Class<T> type) {
    if (isCompatible(input, type)) {
      return input;
    }
    return convert(input, type);
  }

  public static <T extends Annotation> T getAnnotation(AnnotatedElement annotatedElement, Class<T> annotationClass, T defaultInstance) {
    return annotatedElement.isAnnotationPresent(annotationClass) ?
        annotatedElement.getAnnotation(annotationClass) :
        defaultInstance;
  }

  public static List<ObjectMethod> getAnnotatedMethods(Object object, Class<? extends Annotation> annotationClass, Map<String, String> aliases) {
    return Arrays
        .stream(object.getClass().getMethods())
        .filter(each -> each.isAnnotationPresent(annotationClass))
        .map(each -> ObjectMethod.create(object, each, aliases))
        .collect(toList());
  }

  public static Object getFieldValue(Object object, Field field) {
    try {
      return field.get(object);
    } catch (IllegalAccessException e) {
      throw ScriptiveUnitException.wrap(e);
    }
  }

  public static List<ObjectField> getAnnotatedFields(Object object, Class<? extends Annotation> annotationClass) {
    return Arrays
        .stream(object.getClass().getFields())
        .filter(each -> each.isAnnotationPresent(annotationClass))
        .map(each -> ObjectField.create(object, each))
        .collect(toList());
  }

  public static <T> List<T> sort(List<T> list, Comparator<T> comparator) {
    list.sort(comparator);
    return list;
  }

  public static String indent(int level) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < level; i++) {
      b.append("  ");
    }
    return b.toString();
  }

  public static InputStream openResourceAsStream(String resourceName) {
    return requireNonNull(getSystemResourceAsStream(resourceName), format("Failed to open '%s'. Make sure it is available on your classpath.", resourceName));
  }

  public static JsonNode readJsonNodeFromStream(InputStream is) {
    try {
      return new ObjectMapper().readTree(is);
    } catch (IOException e) {
      throw ScriptiveUnitException.wrap(e, "Non-welformed input is given.");
    }
  }

  public static String iterableToString(Iterable<?> i) {
    if (Iterables.size(i) < 2) {
      return i.toString();
    }
    StringBuilder b = new StringBuilder();
    b.append("[\n");
    i.forEach((Object in) -> {
      b.append("  ");
      b.append(in);
      b.append("\n");
    });
    b.append("]");
    return b.toString();
  }

  private interface Converter<FROM, TO> extends Function<FROM, TO> {
    boolean supports(Object input, Class<?> to);

    static <FROM, TO> Converter<FROM, TO> create(Class<FROM> fromClass, Class<TO> toClass, Function<FROM, TO> conveterBody) {
      return new Converter<FROM, TO>() {
        @Override
        public boolean supports(Object input, Class<?> to) {
          return fromClass.isAssignableFrom(input.getClass()) && toClass.isAssignableFrom(wrap(to));
        }

        @Override
        public TO apply(FROM from) {
          return conveterBody.apply(from);
        }
      };
    }
  }

  private static final List<Converter<?, ?>> TYPE_CONVERTERS = new ImmutableList.Builder<Converter<?, ?>>()
      .add(Converter.create(BigDecimal.class, Integer.class, BigDecimal::intValue))
      .add(Converter.create(Number.class, BigDecimal.class, (Number input) -> new BigDecimal(input.toString(), DECIMAL128)))
      .build();
}
