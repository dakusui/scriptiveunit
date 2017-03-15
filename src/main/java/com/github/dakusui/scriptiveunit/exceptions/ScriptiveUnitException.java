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

  protected static <E extends ScriptiveUnitException, T> T validate(Function<String, E> exceptionFactory, Supplier<T> target, Predicate/*<T>*/... predicates) {
    Validator<T> validator = Validator.create(target, predicates);
    if (validator.getAsBoolean())
      return target.get();
    throw exceptionFactory.apply(validator.toString());
  }

  interface Validator<T> extends BooleanSupplier {
    static <T> Validator<T> create(Supplier<T> target, Predicate/*<T>*/... predicates) {
      return new Validator<T>() {
        T object = target.get();

        @Override
        public boolean getAsBoolean() {
          //noinspection unchecked
          for (Predicate<T> each : predicates) {
            if (each.test(object))
              return false;
          }
          return true;
        }

        @Override
        public String toString() {
          StringBuilder builder = new StringBuilder();
          boolean failureAlreadyFound = false;
          //noinspection unchecked
          for (Predicate<T> each : predicates) {
            String keyword;
            if (each.test(object)) {
              builder.append(format("[OK] %s%n", each));
            } else {
              if (!failureAlreadyFound) {
                keyword = "NG";
              } else {
                keyword = "--";
              }
              builder.append(format("[%s] %s (%s)%n", keyword, object, each));
              failureAlreadyFound = true;
            }

          }
          return builder.toString();
        }
      };
    }
  }
}
