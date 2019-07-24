package com.github.dakusui.scriptiveunit.exceptions;

import static java.util.Objects.requireNonNull;

public abstract class ScriptiveUnitException extends RuntimeException {

  public ScriptiveUnitException(String message, Throwable nested) {
    super(message, nested);
  }

  public ScriptiveUnitException(String message) {
    super(message);
  }

  public ScriptiveUnitException(Throwable nested) {
    super(nested);
  }

  public static ScriptiveUnitException wrapMinimally(String message, Throwable t) {
    throw new WrappingException(message, unwrap(t));
  }

  public static ScriptiveUnitException wrapIfNecessary(Throwable t) {
    if (t instanceof RuntimeException)
      throw (RuntimeException) t;
    if (t instanceof Error) {
      throw (Error) t;
    }
    throw new WrappingException(t.getMessage(), requireNonNull(t));
  }

  private static Throwable unwrap(Throwable t) {
    if (t instanceof WrappingException)
      return unwrap(t.getCause());
    return t;
  }

}
