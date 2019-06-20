package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.FormHandle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static java.lang.String.format;

public class FormRegistry {
  private final Map<FormHandle, Form> formMap = new HashMap<>();

  public void register(FormHandle handle, Form form) {
    if (formMap.containsKey(handle))
      throw new IllegalStateException(
          format("Tried to register:%s with the key:%s, but %s was already registered.", form, form.name(), formMap.get(form)));
    formMap.put(handle, form);
  }

  public Optional<Form> lookUp(FormHandle handle) {
    return Optional.ofNullable(formMap.get(handle));
  }

  public class Loader {
    Loader() {
    }


    FormRegistry load() {
      FormRegistry ret = new FormRegistry();

      return ret;
    }
  }


  /**
   * @param object The object to which a method {@code m} belongs.
   * @param m      A method
   * @return A created form created from the given method object.
   */
  public Form loadForm(Object object, Method m) {
    Class<Form>[] params = ensureAllFormClasses(m.getParameterTypes());
    Form[] args = new Form[params.length];
    for (int i = 0; i < params.length; i++) {
      int finalI = i;
      args[i] = (Form) Proxy.newProxyInstance(Form.class.getClassLoader(), new Class[] { Form.class }, (proxy, method, args1) -> {
        Stage stage = (Stage) requireThat(args1, arrayLengthIsOneAndTheElementIsStage(), throwRuntimeException())[0];
        return stage.ongoingStatement().getArguments().get(finalI);
      });
    }

    try {
      return (Form) m.invoke(object, (Object[]) args);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private Class<Form>[] ensureAllFormClasses(Class<?>[] parameterTypes) {
    for (Class<?> each : parameterTypes)
      requireThat(each, isAssignableFrom(Form.class), throwRuntimeException());
    return (Class<Form>[]) parameterTypes;
  }

  private Predicate<Class<?>> isAssignableFrom(Class<Form> formClass) {
    return null;
  }

  private static Predicate<Object[]> arrayLengthIsOneAndTheElementIsStage() {
    return args -> args.length == 1 && args[0] instanceof Stage;
  }

  private static <V> BiFunction<V, Predicate<V>, RuntimeException> throwRuntimeException() {
    return  (V v, Predicate<V> requirement) -> {
      throw new RuntimeException(String.format("Given value:'%s' did not satisfy the requirement:'%s'", v, requirement));
    };
  }

  private static <V> V requireThat(V value, Predicate<V> requirement, BiFunction<V, Predicate<V>, RuntimeException> otherwiseThrow) {
    if (requirement.test(value))
      return value;
    throw otherwiseThrow.apply(value, requirement);
  }
}
