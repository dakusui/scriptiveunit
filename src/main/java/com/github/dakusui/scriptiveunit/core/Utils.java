package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.scriptiveunit.exceptions.ResourceException;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.cyclicTemplatingFound;
import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.undefinedFactor;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.lang.String.format;
import static java.math.MathContext.DECIMAL128;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public enum Utils {
  ;

  public static Runnable prettify(String prettyString, Runnable runnable) {
    return new Runnable() {
      @Override
      public void run() {
        runnable.run();
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static <T> Supplier<T> prettify(String prettyString, Supplier<T> supplier) {
    return new Supplier<T>() {
      @Override
      public T get() {
        return supplier.get();
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static <T> Source<T> prettify(String prettyString, Source<T> source) {
    return new Source<T>() {
      @Override
      public T apply(Context context) {
        return source.apply(context);
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static <T, U> Pipe<T, U> prettify(String prettyString, Pipe<T, U> pipe) {
    return new Pipe<T, U>() {

      @Override
      public U apply(T t, Context context) {
        return pipe.apply(t, context);
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static <T> Predicate<T> prettify(String prettyString, Predicate<T> predicate) {
    return new Predicate<T>() {

      @Override
      public boolean test(T t) {
        return predicate.test(t);
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static <T> Sink<T> prettify(String prettyString, Sink<T> sink) {
    return new Sink<T>() {

      @Override
      public void apply(T t, Context context) {
        sink.apply(t, context);
      }

      @Override
      public String toString() {
        return prettyString;
      }
    };
  }

  public static String template(String s, Map<String, Object> map) {
    String ret = s;
    Pattern pattern = Pattern.compile("\\{\\{(?<keyword>@?[A-Za-z_][A-Za-z0-9_]*)}}");
    Matcher matcher;
    int i = 0;
    while ((matcher = pattern.matcher(ret)).find()) {
      String keyword = matcher.group("keyword");
      check(i++ < map.size(), () -> cyclicTemplatingFound(format("template(%s)", s), map));
      check(map.containsKey(keyword), () -> undefinedFactor(keyword, format("template(%s)", s)));
      ret = ret.replaceAll(format("\\{\\{%s\\}\\}", keyword), requireNonNull(map.get(keyword)).toString());
    }
    return ret;
  }

  public static Tuple filterSimpleSingleLevelParametersOut(Tuple tuple, List<Parameter> factors) {
    Tuple.Builder b = new Tuple.Builder();
    factors.stream().filter(each -> !(each instanceof Parameter.Simple))
        .filter(each -> each.getKnownValues().size() > 1)
        .filter(each -> tuple.containsKey(each.getName()))
        .forEach(each -> b.put(each.getName(), tuple.get(each.getName())));
    return b.build();
  }

  public static Tuple append(Tuple tuple, String key, Object value) {
    Tuple.Builder b = new Tuple.Builder();
    b.putAll(tuple);
    b.put(key, value);
    return b.build();
  }

  public static void performActionWithLogging(Action action) {
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter(ActionPrinter.Writer.Slf4J.TRACE));
    }
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

  public static <E extends ScriptiveUnitException> void check(boolean cond, Supplier<E> thrower) {
    if (!cond)
      throw thrower.get();
  }

  public static <E extends ScriptiveUnitException, V> V check(V target, Predicate<? super V> predicate,
      Supplier<? extends E> thrower) {
    if (!requireNonNull(predicate).test(target))
      throw thrower.get();
    return target;
  }

  public static <V> V check(V target, Predicate<? super V> predicate, String fmt, Object... args) {
    if (!requireNonNull(predicate).test(target))
      throw new ScriptiveUnitException(String.format(fmt, args));
    return target;
  }


  public static BigDecimal toBigDecimal(Number number) {
    if (number instanceof BigDecimal)
      return (BigDecimal) number;
    return new BigDecimal(number.toString(), DECIMAL128);
  }

  public static Object toBigDecimalIfPossible(Object object) {
    if (object instanceof Number) {
      return toBigDecimal((Number) object);
    }
    return object;
  }

  // safe because both Long.class and long.class are of type Class<Long>
  @SuppressWarnings("unchecked")
  public static <T> Class<T> wrap(Class<T> c) {
    return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
  }

  private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new ImmutableMap.Builder<Class<?>, Class<?>>()
      .put(boolean.class, Boolean.class).put(byte.class, Byte.class).put(char.class, Character.class)
      .put(double.class, Double.class).put(float.class, Float.class).put(int.class, Integer.class)
      .put(long.class, Long.class).put(short.class, Short.class).put(void.class, Void.class).build();

  /**
   * Returns an annotation element of a specified type ({@code annotationClass})
   * attached to {@code annotatedElement}.
   * If it is not present, {@code defaultInstance} will be returned.
   *
   * @param annotatedElement An element from which annotation object to be returned is retrieved.
   * @param annotationClass  An annotation class of the instance to be returned.
   * @param defaultInstance  An annotation object to be returned in the {@code annotatedElement} doesn't have it.
   */
  public static <T extends Annotation> T getAnnotation(AnnotatedElement annotatedElement, Class<T> annotationClass,
      T defaultInstance) {
    return annotatedElement.isAnnotationPresent(annotationClass) ?
        annotatedElement.getAnnotation(annotationClass) :
        defaultInstance;
  }

  public static List<ObjectMethod> getAnnotatedMethods(Object object, Class<? extends Annotation> annotationClass,
      Map<String, String> aliases) {
    return Arrays.stream(object.getClass().getMethods()).filter(each -> each.isAnnotationPresent(annotationClass))
        .map(each -> ObjectMethod.create(object, each, aliases)).collect(toList());
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

  public static InputStream openResourceAsStream(String resourceName) {
    return ResourceException.scriptExists(getSystemResourceAsStream(resourceName), resourceName);
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

  public static Stream<String> allScriptsUnder(String prefix) {
    return allScriptsUnderMatching(prefix, Pattern.compile(".*"));
  }

  public static Stream<String> allScriptsUnderMatching(String prefix, Pattern pattern) {
    return new Reflections(prefix, new ResourcesScanner()).getResources(pattern).stream();
  }

  public static Stream<Class<?>> allTypesAnnotatedWith(String prefix, Class<? extends Annotation> annotation) {
    return findEntitiesUnder(prefix, (Reflections reflections) -> reflections.getTypesAnnotatedWith(annotation));
  }

  public static Stream<Class<?>> findEntitiesUnder(String prefix, Function<Reflections, Set<Class<?>>> func) {
    return func.apply(new Reflections(prefix, new TypeAnnotationsScanner(), new SubTypesScanner())).stream();
  }

  public static <T extends Annotation> T getAnnotationWithDefault(Class javaClass, T defaultValue) {
    @SuppressWarnings("unchecked") T ret = (T) javaClass.<T>getAnnotation(defaultValue.annotationType());
    return ret != null ?
        ret :
        defaultValue;
  }

  /**
   * Extract all the factors whose values are not changed among in the test cases.
   *
   * @param parameters A list of all the parameters used  in the test suite
   * @return A tuple that holds unmodified factors and their values.
   */
  public static Tuple createCommonFixture(List<Parameter> parameters) {
    Tuple.Builder b = new Tuple.Builder();
    parameters.stream()
        .filter((Parameter in) -> in instanceof Parameter.Simple)
        .filter((Parameter in) -> in.getKnownValues().size() == 1)
        .forEach((Parameter in) -> b.put(in.getName(), in.getKnownValues().get(0)));
    return b.build();
  }
}
