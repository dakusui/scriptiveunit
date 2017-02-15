package com.github.dakusui.scriptunit.exceptions;

import com.github.dakusui.scriptunit.model.func.Func;

import static java.lang.String.format;

public class TypeMismatch extends ScriptUnitException {
  private TypeMismatch(String format, Object... args) {
    super(format(format, args));
  }

  public static TypeMismatch valueReturnedByScriptableMethodMustBeFunc(String methodName, Object returnedValue) {
    throw new TypeMismatch("Value '%s' returned by '%s' must be an instance of '%s'", returnedValue, methodName, Func.class.getCanonicalName());
  }
}
