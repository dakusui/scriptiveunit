package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.exceptions.ValidationFailure;
import com.github.dakusui.scriptiveunit.runners.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.scriptiveunit.utils.StringUtils.prettify;
import static java.lang.String.format;

public interface Validator extends BooleanSupplier {
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
      @SuppressWarnings("unchecked")
      @Override
      public boolean getAsBoolean() {
        for (Predicate<T> each : predicates) {
          if (!each.test(target))
            return false;
        }
        return true;
      }

      @SuppressWarnings("unchecked")
      @Override
      public String toString() {
        StringBuilder builder = new StringBuilder(format(format, target));
        builder.append(format("%n"));
        boolean failureAlreadyFound = false;
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

  static Class<?> validateDriverClass(Class<?> driverClass) {
    return validate(
        "Validates '%s' as a driver class.",
        ValidationFailure::new,
        driverClass,
        Objects::nonNull,
        aClass -> driverClass.isAnnotationPresent(RunWith.class),
        (Predicate<Class<?>>) aClass -> driverClass.getAnnotation(RunWith.class).value().equals(ScriptiveUnit.class),
        (Predicate<Class<?>>) Validator::hasPublicConstructor,
        (Predicate<Class<?>>) Validator::constructorHasNoParameter
    );
  }

  static Class<?> validateSuiteSetClass(Class<?> suiteSetClass) {
    return validate(
        "Validates '%s' as a suite set class.",
        ValidationFailure::new,
        suiteSetClass,
        prettify("is non-null", (Predicate<?>) Objects::nonNull),
        prettify("has @RunWith", (Class<?> aClass) -> suiteSetClass.isAnnotationPresent(RunWith.class)),
        prettify("has ScriptiveSuiteSet.class as value of @RunWith", (Class<?> aClass) -> suiteSetClass.getAnnotation(RunWith.class).value().equals(ScriptiveSuiteSet.class)),
        prettify("has @SuiteScripts", (Class<?> aClass) -> aClass.isAnnotationPresent(ScriptiveSuiteSet.SuiteScripts.class)),
        prettify("has one and only one public constructor", Validator::hasPublicConstructor),
        prettify("only one constructor has no parameter", Validator::constructorHasNoParameter)
    );
  }

  static boolean hasPublicConstructor(Class<?> driverClass) {
    return driverClass.getConstructors().length == 1;
  }

  static boolean constructorHasNoParameter(Class<?> driverClass) {
    return driverClass.getConstructors()[0].getParameterTypes().length == 0;
  }

  static <E extends ScriptiveUnitException, T> T validate(String format, Function<String, E> exceptionFactory, T target, Predicate/*<T>*/... predicates) {
    Validator validator = create(format, target, predicates);
    if (validator.getAsBoolean())
      return target;
    throw exceptionFactory.apply(validator.toString());
  }
}
