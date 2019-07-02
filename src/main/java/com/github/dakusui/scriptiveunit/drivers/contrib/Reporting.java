package com.github.dakusui.scriptiveunit.drivers.contrib;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.Stage;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;

public class Reporting {
  @SuppressWarnings("unused")
  @Scriptable
  public Form<Object> write_report(Form<String> name, Form<Object> value) {
    return input -> {
      Object ret;
      input.getReport()
          .orElseThrow(RuntimeException::new)
          .put(name.apply(input), ret = value.apply(input));
      return ret;
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> submit() {
    return (Stage input) -> simple("submit",
        (c) -> input.getReport()
            .orElseThrow(RuntimeException::new).submit());
  }
}
