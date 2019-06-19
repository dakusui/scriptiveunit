package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.FormHandle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

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


  public Form loadForm(Method m) {
    Form target = null;
    Form arg = (Form) Proxy.newProxyInstance(
        Form.class.getClassLoader(),
        new Class[] { Form.class },
        new InvocationHandler() {

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!"apply".equals(method.getName()))
              return method.invoke(target, args);
            Stage stage = (Stage) requireThat(args, arrayLengthIsOneAndTheElementIsStage(), throwRuntimeException())[0];
            return null;
            //return invoker.invokeForm(target, stage, name);
            //return formHandler.handleForm(invoker, target, (Stage) args[0], name);
          }
        }
    );
    try {
      return (Form) m.invoke(null, arg);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private static Predicate<Object[]> arrayLengthIsOneAndTheElementIsStage() {
    return args -> args.length == 1 && args[0] instanceof Stage;
  }

  private static Supplier<RuntimeException> throwRuntimeException() {
    throw new RuntimeException();
  }

  private static <V> V requireThat(V value, Predicate<V> requirement, Supplier<RuntimeException> otherwiseThrow) {
    if (requirement.test(value))
      return value;
    throw otherwiseThrow.get();
  }
}
