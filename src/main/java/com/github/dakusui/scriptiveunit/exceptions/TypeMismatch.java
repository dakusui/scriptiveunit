package com.github.dakusui.scriptiveunit.exceptions;

import com.github.dakusui.scriptiveunit.model.form.Form;

import static java.lang.String.format;

public class TypeMismatch extends ScriptiveUnitException {
  private TypeMismatch(String format, Object... args) {
    super(format(format, args));
  }

  public static TypeMismatch valueReturnedByScriptableMethodMustBeFunc(String methodName, Object returnedValue) {
    throw new TypeMismatch("Value '%s' returned by '%s' must be an instance of '%s'", returnedValue, methodName, Form.class.getCanonicalName());
  }

  public static TypeMismatch headOfCallMustBeString(Object car) {
    throw new TypeMismatch("Head of a call must be a string but '%s' as given", car);
  }
}
