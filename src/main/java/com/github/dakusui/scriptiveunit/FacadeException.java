package com.github.dakusui.scriptiveunit;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.function.Predicate;

public class FacadeException extends ScriptiveUnitException {
  private FacadeException(String message) {
    super(message);
  }

  public static Class<?> validateDriverClass(Class<?> driverClass) {
    return validate(
        FacadeException::new,
        () -> driverClass,
        Objects::nonNull,
        aClass -> driverClass.isAnnotationPresent(RunWith.class),
        (Predicate<Class<?>>) aClass -> driverClass.getAnnotation(RunWith.class).value().equals(ScriptiveUnit.class),
        (Predicate<Class<?>>) FacadeException::hasPublicConstructor,
        (Predicate<Class<?>>) FacadeException::constructorHasNoParameter
    );
  }

  private static boolean hasPublicConstructor(Class<?> driverClass) {
    return driverClass.getConstructors().length == 1;
  }

  private static boolean constructorHasNoParameter(Class<?> driverClass) {
    return driverClass.getConstructors()[0].getParameterTypes().length == 0;
  }
}
