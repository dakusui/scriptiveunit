package com.github.dakusui.scriptiveunit.model.form;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

public class FormRegistry {
  private final Map<String, Form> formMap = new HashMap<>();

  public void register(Form form) {
    if (formMap.containsKey(form.name()))
      throw new IllegalStateException(
          format("Tried to register:%s with the key:%s, but %s was already registered.", form, form.name(), formMap.get(form.name())));
    formMap.put(form.name(), form);
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
        new Class[] { Form.class },
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
