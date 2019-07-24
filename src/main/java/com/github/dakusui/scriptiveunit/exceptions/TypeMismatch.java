package com.github.dakusui.scriptiveunit.exceptions;

import com.github.dakusui.scriptiveunit.model.form.value.Value;

import static java.lang.String.format;

public class TypeMismatch extends ScriptiveUnitException {
  TypeMismatch(String format, Object... args) {
    super(format(format, args));
  }

}
