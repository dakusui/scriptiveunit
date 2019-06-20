package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Arguments;
import com.github.dakusui.scriptiveunit.model.statement.FormHandle;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.dakusui.scriptiveunit.model.form.FormRegistry.Utils.toArgs;
import static java.lang.String.format;

public interface FormRegistry {
  Optional<Form> lookUp(FormHandle handle);

  class Loader {
    private final Map<FormHandle, Form> formMap = new HashMap<>();

    public void register(FormHandle handle, Form form) {
      if (formMap.containsKey(handle))
        throw new IllegalStateException(
            format("Tried to register:%s with the key:%s, but %s was already registered.", form, form.name(), formMap.get(form)));
      formMap.put(handle, form);
    }

    FormRegistry load() {
      return handle -> Optional.ofNullable(formMap.get(handle));
    }

    /**
     * @param object The object to which a method {@code m} belongs.
     * @param m      A method
     * @return A created form created from the given method object.
     */
    public Form loadForm(Object object, Method m) {
      Class<Form>[] params = Utils.ensureAllFormClasses(m.getParameterTypes());
      Object[] args = toArgs(params);

      try {
        return (Form) m.invoke(object, args);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

  enum Utils {
    ;

    static Form[] toArgs(Class<Form>[] params) {
      return new ArrayList<Form>(params.length) {
        {
          for (int i = 0; i < params.length; i++) {
            this.add(createProxiedFormForArgumentAt(this.size()));
          }
        }
      }.toArray(new Form[params.length]);
    }

    private static Form createProxiedFormForArgumentAt(int i) {
      return (Form) Proxy.newProxyInstance(
          Form.class.getClassLoader(),
          new Class[] { Form.class },
          (proxy, method, args) -> {
            Stage stage = (Stage) requireThat(args, arrayLengthIsOneAndTheElementIsStage(), otherwiseThrowRuntimeException())[0];
            Arguments arguments = stage.ongoingStatement().getArguments();
            Statement statement = arguments.get(i);
            return statement.evaluate(stage);
          });
    }

    @SuppressWarnings("unchecked")
    private static Class<Form>[] ensureAllFormClasses(Class<?>[] parameterTypes) {
      for (Class<?> each : parameterTypes)
        requireThat(each, isAssignableFrom(Form.class), otherwiseThrowRuntimeException());
      return (Class<Form>[]) parameterTypes;
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> Predicate<Class<?>> isAssignableFrom(Class<T> formClass) {
      return formClass::isAssignableFrom;
    }

    private static Predicate<Object[]> arrayLengthIsOneAndTheElementIsStage() {
      return args -> args.length == 1 && args[0] instanceof Stage;
    }

    private static <V> BiFunction<V, Predicate<V>, RuntimeException> otherwiseThrowRuntimeException() {
      return (V v, Predicate<V> requirement) -> {
        throw new RuntimeException(format("Given value:'%s' did not satisfy the requirement:'%s'", v, requirement));
      };
    }

    private static <V> V requireThat(V value, Predicate<V> requirement, BiFunction<V, Predicate<V>, RuntimeException> otherwiseThrow) {
      if (requirement.test(value))
        return value;
      throw otherwiseThrow.apply(value, requirement);
    }

    public static Supplier<RuntimeException> undefinedFormError(Statement.Nested nestedStatement) {
      return () -> {
        throw new UnsupportedOperationException(format("Undefined form:'%s' was requested", nestedStatement.getFormHandle()));
      };
    }
  }
}
