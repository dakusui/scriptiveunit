package com.github.dakusui.scriptiveunit.exceptions;

import java.util.function.Supplier;

import static java.lang.String.format;
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

  public static ScriptiveUnitException wrap(Throwable t, String format, Object... args) {
    throw new ScriptiveUnitException(format(format, (Object[]) args), requireNonNull(t));
  }

  public static ScriptiveUnitException wrap(Throwable t) {
    throw new ScriptiveUnitException(requireNonNull(t));
  }

  public static Supplier<ScriptiveUnitException> fail(String fmt, Object... args) {
    return () -> {
      throw new ScriptiveUnitException(format(fmt, args));
    };
  }

  public static ScriptiveUnitException indexOutOfBounds(int index, int size) {
    return new ScriptiveUnitException(format("%sth element was accessed but the container's length was %s", index, size));
  }
}
