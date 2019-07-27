package com.github.dakusui.scriptiveunit.exceptions;

public class ScriptiveUnitUnclassifiedException extends ScriptiveUnitException {
  public ScriptiveUnitUnclassifiedException(String message) {
    super(message);
  }

  public static ScriptiveUnitException unclassifiedException(String format, Object... args) {
    return new ScriptiveUnitUnclassifiedException(String.format(format, args));
  }
}
