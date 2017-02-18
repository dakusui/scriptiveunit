package com.github.dakusui.scriptiveunit.exceptions;

import static java.util.Objects.requireNonNull;

public class ScriptiveUnitException extends RuntimeException {
  public ScriptiveUnitException(String message, Throwable nested) {
    super(message, nested);
  }

  public ScriptiveUnitException(String message) {
    super(message);
  }

  public ScriptiveUnitException(Throwable nested) {
    super(nested);
  }

  public static ScriptiveUnitException wrap(Throwable t, String format, String... args) {
    throw new ScriptiveUnitException(String.format(format, (Object[]) args), requireNonNull(t));
  }

  public static ScriptiveUnitException wrap(Throwable t) {
    throw new ScriptiveUnitException(requireNonNull(t));
  }

}
