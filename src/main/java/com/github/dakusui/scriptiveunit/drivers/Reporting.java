package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;

import static com.github.dakusui.actionunit.Actions.simple;
import static com.github.dakusui.scriptiveunit.core.Utils.prettify;

public class Reporting {
  @SuppressWarnings("unused")
  @Scriptable
  public Func<Object> write_report(Func<String> name, Func<Object> value) {
    return new Func<Object>() {
      @Override
      public Object apply(Stage input) {
        Object ret;
        input.getReport().put(name.apply(input), ret = value.apply(input));
        return ret;
      }
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Func<Action> submit() {
    return (Stage input) -> simple(prettify("submit", () -> {
      input.getReport().submit();
    }));
  }
}
