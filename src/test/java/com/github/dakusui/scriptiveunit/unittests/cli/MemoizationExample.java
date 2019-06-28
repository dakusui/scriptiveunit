package com.github.dakusui.scriptiveunit.unittests.cli;

import com.github.dakusui.scriptiveunit.annotations.Memoized;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.Func;

public class MemoizationExample {
  private int increment = 0;

  @Scriptable
  public final Func<Integer> increment() {
    return new Func.Builder<Integer>()
        .func(any -> increment++)
        .build();
  }

  private int op = 0;

  @Memoized
  @Scriptable
  public final Func<Integer> op(Form<Integer> a, Form<Integer> b) {
    return Func.body(objects -> (Integer) objects[0] + (Integer) objects[1] + op++)
        .addParameter(a)
        .addParameter(b)
        .$();

  }
}
