package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.annotations.AccessesTestParameter;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Memoized;
import com.github.dakusui.scriptiveunit.core.Description;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.model.form.value.FuncValue;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import com.github.dakusui.scriptiveunit.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.exceptions.TypeMismatch.valueReturnedByScriptableMethodWasNotValueObject;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * An interface that represents a pair of a method and object on which it should
 * be invoked.
 */
public interface Form {
  /*
   * args is an array can only contain Value or Value[]. Only the last element in it
   * can become Value[] it is because only the last argument of a method can become
   * a varargs.
   */
  <V> Value<V> resolveValue(Value[] args);

  String getName();

  int getParameterCount();

  Class<?>[] getParameterTypes();

  Doc getParameterDoc(int index);

  Doc doc();

  /**
   * Returns {@code true} if this object represents an "accessor", which retrieves a data in
   * a test case tuple.
   *
   * @return {@code true} - this object is an accessor / {@code false} - otherwise.
   */
  boolean isAccessor();

  static Form create(Object driverObject, Method method, Map<String, String> aliases) {
    String baseName = method.getName();
    String methodName = aliases.containsKey(baseName) ?
        aliases.get(baseName) :
        aliases.containsKey(Import.Alias.ALL) ?
            baseName :
            null;
    Map<List<Object>, Object> memo;
    if (method.isAnnotationPresent(Memoized.class)) {
      requireReturnedValueAssignableToFunc(method);
      memo = new HashMap<>();
    } else
      memo = null;

    return new Form() {
      private final Class<?>[] parameterTypes = method.getParameterTypes();

      @Override
      public String getName() {
        return methodName;
      }

      @Override
      public int getParameterCount() {
        return this.getParameterTypes().length;
      }

      @Override
      public Class<?>[] getParameterTypes() {
        return parameterTypes;
      }

      @Override
      public Doc getParameterDoc(int index) {
        return stream(method.getParameterAnnotations()[index])
            .filter(input -> input instanceof Doc)
            .findFirst()
            .map(annotation -> (Doc) annotation)
            .orElse(Doc.NOT_AVAILABLE);
      }

      /*
       * args is an array can only contain Form or Form[]. Only the last element in it
       * can become Form[] it is because only the last argument of a method can become
       * a varargs.
       */
      @SuppressWarnings("unchecked")
      @Override
      public <V> Value<V> resolveValue(Value[] args) {
        Object returnedValue = this.invokeMethod(composeArgs(args));
        /*
         * By using dynamic proxy, we are making it possible to print structured pretty log.
         */
        return createValue(requireReturnedValueIsValueObject(returnedValue));
      }

      private Value requireReturnedValueIsValueObject(Object returnedValue) {
        return (Value) check(
            returnedValue,
            (Object o) -> o instanceof Value,
            () -> valueReturnedByScriptableMethodWasNotValueObject(this.getName(), returnedValue)
        );
      }

      @Override
      public boolean isAccessor() {
        return method.isAnnotationPresent(AccessesTestParameter.class);
      }

      @Override
      public Doc doc() {
        return method.isAnnotationPresent(Doc.class) ?
            method.getAnnotation(Doc.class) :
            Doc.NOT_AVAILABLE;
      }

      @Override
      public String toString() {
        return String.format("%s(%s) in class:<%s>", method.getName(),
            stream(method.getParameterTypes())
                .map(t -> Arrays.stream(method.getGenericParameterTypes())
                    .map(Type::getTypeName)
                    .map(s -> String.format("%s%s", Value.class.getSimpleName(), s.substring(s.indexOf('<'))))
                    .collect(joining(",")))
                .collect(joining(",")),
            driverObject.getClass().getCanonicalName());
      }

      Object invokeMethod(Object... args) {
        try {
          return method.invoke(driverObject, args);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
          String message = format("Failed to invoke %s#%s(%s) with %s",
              method.getDeclaringClass().getCanonicalName(),
              method.getName(),
              StringUtils.arrayToString(parameterTypes),
              StringUtils.arrayToString(args));
          throw ScriptiveUnitException.wrapMinimally(message, e);
        }
      }

      boolean isVarargs() {
        return parameterTypes.length > 0 && ValueList.class.isAssignableFrom(parameterTypes[parameterTypes.length - 1]);
      }

      <T> Object[] composeArgs(Value<T>[] args) {
        Object[] argValues;
        if (isVarargs()) {
          int parameterCount = this.getParameterCount();
          List<Object> work = new ArrayList<>(args.length);
          work.addAll(asList(args).subList(0, parameterCount - 1));
          work.add(ValueList.create(asList(args).subList(parameterCount - 1, args.length)));
          argValues = work.toArray();
        } else
          argValues = args;
        return argValues;
      }

      <V> Value<V> createValue(Value<V> target_) {
        if (memo == null)
          return target_;
        return new FuncValue<V>() {
          FuncValue<V> target = (FuncValue<V>) target_;

          @Override
          public List<Value> parameters() {
            return target.parameters();
          }

          @Override
          public V apply(Stage input) {
            return body().apply(parameters()
                .stream()
                .map(param -> param.apply(input))
                .toArray());
          }

          @SuppressWarnings("unchecked")
          @Override
          public Function<Object[], V> body() {
            return (Object[] args) -> (V) memo.computeIfAbsent(
                asList(args),
                objects -> target.body().apply(objects.toArray()));
          }
        };
      }

    };
  }

  static void requireReturnedValueAssignableToFunc(Method method) {
    check(
        method,
        m -> FuncValue.class.isAssignableFrom(m.getReturnType()),
        "'%s' is expected to return a class assignable to '%s'", method,
        FuncValue.class.getCanonicalName());
  }

  static Description describe(Form form) {
    requireNonNull(form);
    return new Description() {
      @Override
      public String name() {
        return form.getName();
      }

      @Override
      public List<String> content() {
        return asList(form.doc().value());
      }

      @Override
      public List<Description> children() {
        return new AbstractList<Description>() {
          @Override
          public Description get(int index) {
            return new Description() {
              @Override
              public String name() {
                return String.format("[%d] %s", index, form.getParameterTypes()[index].getName());
              }

              @Override
              public List<String> content() {
                return asList(form.getParameterDoc(index).value());
              }

              @Override
              public String toString() {
                return name();
              }
            };
          }

          @Override
          public int size() {
            return form.getParameterCount();
          }
        };
      }

      @Override
      public String toString() {
        return name();
      }
    };
  }
}
