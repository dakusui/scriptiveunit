package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.annotations.AccessesTestParameter;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Memoized;
import com.github.dakusui.scriptiveunit.core.Description;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormList;
import com.github.dakusui.scriptiveunit.model.form.Func;
import com.github.dakusui.scriptiveunit.model.session.Stage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.exceptions.TypeMismatch.valueReturnedByScriptableMethodMustBeFunc;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

/**
 * An interface that represents a pair of a method and object on which it should
 * be invoked.
 */
public interface ObjectMethod {
  /*
   * args is an array can only contain Form or Form[]. Only the last element in it
   * can become Form[] it is because only the last argument of a method can become
   * a varargs.
   */
  <V> Form<V> createForm(Form[] args);

  String getName();

  int getParameterCount();

  Class<?>[] getParameterTypes();

  Doc getParameterDoc(int index);

  Doc doc();

  boolean isAccessor();

  static ObjectMethod create(Object driverObject, Method method, Map<String, String> aliases) {
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

    return new ObjectMethod() {
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
      public <V> Form<V> createForm(Form[] args) {
        Object returnedValue = this.invokeMethod(composeArgs(args));
        /*
         * By using dynamic proxy, we are making it possible to print structured pretty log.
         */
        return createForm(requireValueIsForm(returnedValue));
      }

      private Form requireValueIsForm(Object returnedValue) {
        return (Form) check(
            returnedValue,
            (Object o) -> o instanceof Form,
            () -> valueReturnedByScriptableMethodMustBeFunc(this.getName(), returnedValue)
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
        return String.format("%s(%s of %s)", method.getName(), method, driverObject);
      }

      Object invokeMethod(Object... args) {
        try {
          return method.invoke(driverObject, args);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
          String message = format("Failed to invoke %s#%s(%s) with %s",
              method.getDeclaringClass().getCanonicalName(),
              method.getName(),
              arrayToString(parameterTypes),
              arrayToString(args));
          throw wrap(e, message);
        }
      }

      boolean isVarargs() {
        return parameterTypes.length > 0 && FormList.class.isAssignableFrom(parameterTypes[parameterTypes.length - 1]);
      }

      <T> Object[] composeArgs(Form<T>[] args) {
        Object[] argValues;
        if (isVarargs()) {
          int parameterCount = this.getParameterCount();
          List<Object> work = new ArrayList<>(args.length);
          work.addAll(asList(args).subList(0, parameterCount - 1));
          work.add(FormList.create(asList(args).subList(parameterCount - 1, args.length)));
          argValues = work.toArray();
        } else
          argValues = args;
        return argValues;
      }

      <V> Form<V> createForm(Form<V> target_) {
        if (memo == null)
          return target_;
        return new Func<V>() {
          Func<V> target = (Func<V>) target_;

          @Override
          public List<Form> parameters() {
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

      String arrayToString(Object[] args) {
        try {
          return Arrays.toString(args);
        } catch (Exception e) {
          return "(N/A)";
        }
      }
    };
  }

  static void requireReturnedValueAssignableToFunc(Method method) {
    check(
        method,
        m -> Func.class.isAssignableFrom(m.getReturnType()),
        "'%s' is expected to return a class assignable to '%s'", method,
        Func.class.getCanonicalName());
  }

  static Description describe(ObjectMethod objectMethod) {
    requireNonNull(objectMethod);
    return new Description() {
      @Override
      public String name() {
        return objectMethod.getName();
      }

      @Override
      public List<String> content() {
        return asList(objectMethod.doc().value());
      }

      @Override
      public List<Description> children() {
        return new AbstractList<Description>() {
          @Override
          public Description get(int index) {
            return new Description() {
              @Override
              public String name() {
                return String.format("[%d] %s", index, objectMethod.getParameterTypes()[index].getName());
              }

              @Override
              public List<String> content() {
                return asList(objectMethod.getParameterDoc(index).value());
              }

              @Override
              public String toString() {
                return name();
              }
            };
          }

          @Override
          public int size() {
            return objectMethod.getParameterCount();
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
