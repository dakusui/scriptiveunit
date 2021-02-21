package com.github.dakusui.scriptiveunit.unittests.cli;

import com.github.dakusui.scriptiveunit.annotations.Memoized;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.FuncValue;

public class MemoizationExample {
  private int increment = 0;

  @Scriptable
  public final FuncValue<Integer> increment() {
    return new FuncValue.Builder<Integer>()
        .func(any -> increment++)
        .build();
  }

  private int op = 0;

  @Memoized
  @Scriptable
  public final FuncValue<Integer> op(Value<Integer> a, Value<Integer> b) {
    return FuncValue.body(objects -> (Integer) objects[0] + (Integer) objects[1] + op++)
        .addParameter(a)
        .addParameter(b)
        .$();

  }
}
