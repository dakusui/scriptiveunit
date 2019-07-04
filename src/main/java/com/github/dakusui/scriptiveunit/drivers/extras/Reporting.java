package com.github.dakusui.scriptiveunit.drivers.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Form;

public class Reporting extends DataStore {
  @SuppressWarnings("unused")
  @Scriptable
  public Form<Object> write_report(Form<String> name, Form<Object> value) {
    return put(name, value);
  }
}
