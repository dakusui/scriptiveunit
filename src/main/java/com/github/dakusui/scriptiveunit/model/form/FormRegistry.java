package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.statement.FormHandle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

public class FormRegistry {
  private final Map<FormHandle, Form> formMap = new HashMap<>();

  public void register(FormHandle handle, Form form) {
    if (formMap.containsKey(handle))
      throw new IllegalStateException(
          format("Tried to register:%s with the key:%s, but %s was already registered.", form, form.name(), formMap.get(form.name())));
    formMap.put(handle, form);
  }

  public Optional<Form> lookUp(String name) {
    return Optional.ofNullable(formMap.get(name));
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
    Form arg = (Form) Proxy.newProxyInstance(
        Form.class.getClassLoader(),
        new Class[]{Form.class},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
          }
        }
    );
    try {
      return (Form) m.invoke(null, arg);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  static Form<String> numToString(Form<Integer> arg) {
    return input -> Integer.toString(arg.apply(input));
  }
}
