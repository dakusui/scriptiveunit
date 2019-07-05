package com.github.dakusui.scriptiveunit.exceptions;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
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

  public static ScriptiveUnitException wrapIfNecessary(Throwable t) {
    if (t instanceof RuntimeException)
      throw (RuntimeException) t;
    if (t instanceof Error) {
      throw (Error) t;
    }
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

  static <E extends ScriptiveUnitException, T> T validate(String format, Function<String, E> exceptionFactory, T target, Predicate/*<T>*/... predicates) {
    Validator validator = Validator.create(format, target, predicates);
    if (validator.getAsBoolean())
      return target;
    throw exceptionFactory.apply(validator.toString());
  }

  interface Validator extends BooleanSupplier {
    /**
     * Creates and returns a validator for {@code target} object.
     *
     * @param format     has one and only one '%s' place holder. The value of {@code target#toString()} will be embedded there.
     * @param target     An object to be validated.
     * @param predicates predicates used for validation.
     * @param <T>        Type of target
     */
    static <T> Validator create(String format, T target, Predicate/*<T>*/... predicates) {
      return new Validator() {
        @Override
        public boolean getAsBoolean() {
          //noinspection unchecked
          for (Predicate<T> each : predicates) {
            if (!each.test(target))
              return false;
          }
          return true;
        }

        @Override
        public String toString() {
          StringBuilder builder = new StringBuilder(format(format, target));
          builder.append(format("%n"));
          boolean failureAlreadyFound = false;
          //noinspection unchecked
          for (Predicate<T> each : predicates) {
            if (failureAlreadyFound) {
              builder.append(format("[--] %s%n", each));
            } else {
              if (each.test(target)) {
                builder.append(format("[OK] %s%n", each));
              } else {
                builder.append(format("[NG] %s%n", each));
                failureAlreadyFound = true;
              }
            }
          }
          return builder.toString();
        }
      };
    }
  }
}
