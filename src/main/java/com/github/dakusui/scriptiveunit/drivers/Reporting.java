package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Form;

import static com.github.dakusui.actionunit.Actions.simple;
import static com.github.dakusui.scriptiveunit.core.Utils.prettify;

public class Reporting {
  @SuppressWarnings("unused")
  @Scriptable
  public Form<Object> write_report(Form<String> name, Form<Object> value) {
    return input -> {
      Object ret;
      input.getReport().put(name.apply(input), ret = value.apply(input));
      return ret;
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> submit() {
    return (Stage input) -> simple(prettify("submit", () -> input.getReport().submit()));
  }
}
