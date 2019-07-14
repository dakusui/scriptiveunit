package com.github.dakusui.scriptiveunit.libs.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.value.Value;

public class Reporting extends DataStore {
  @SuppressWarnings("unused")
  @Scriptable
  public Value<Object> write_report(Value<String> name, Value<Object> value) {
    return put(name, value);
  }
}
