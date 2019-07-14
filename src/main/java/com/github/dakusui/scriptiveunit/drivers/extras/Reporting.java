package com.github.dakusui.scriptiveunit.drivers.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Value;

public class Reporting extends DataStore {
  @SuppressWarnings("unused")
  @Scriptable
  public Value<Object> write_report(Value<String> name, Value<Object> value) {
    return put(name, value);
  }
}
