package com.github.dakusui.scriptunit.exceptions;

import static java.util.Objects.requireNonNull;

public class ScriptUnitException extends RuntimeException {
  public ScriptUnitException(String message, Throwable nested) {
    super(message, nested);
  }

  public ScriptUnitException(String message) {
    super(message);
  }

  public ScriptUnitException(Throwable nested) {
    super(nested);
  }

  public static ScriptUnitException wrap(Throwable t, String format, String... args) {
    throw new ScriptUnitException(String.format(format, (Object[]) args), requireNonNull(t));
  }

  public static ScriptUnitException wrap(Throwable t) {
    throw new ScriptUnitException(requireNonNull(t));
  }

}
