package com.github.dakusui.scriptiveunit.drivers.extras;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.Stage;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;

public class DataStore {
  @SuppressWarnings("unused")
  @Scriptable
  public Form<Object> put(Form<String> name, Form<Object> value) {
    return input -> {
      Object ret;
      input.getReport()
          .orElseThrow(RuntimeException::new)
          .put(name.apply(input), ret = value.apply(input));
      return ret;
    };
  }

  @Scriptable
  public Form<Object> get(Form<String> name) {
    return input -> input.getReport()
        .orElseThrow(RuntimeException::new)
        .get(name.apply(input));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> submit() {
    return (Stage input) -> simple("submit",
        (c) -> input.getReport()
            .orElseThrow(RuntimeException::new)
            .submit());
  }
}
