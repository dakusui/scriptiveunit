package com.github.dakusui.scriptiveunit.exceptions;

import com.github.dakusui.scriptiveunit.utils.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.utils.ScriptiveSuiteSet.SuiteScripts;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.function.Predicate;

import static com.github.dakusui.scriptiveunit.utils.StringUtils.prettify;

public class FacadeException extends ScriptiveUnitException {
  private FacadeException(String message) {
    super(message);
  }

  public static Class<?> validateDriverClass(Class<?> driverClass) {
    return validate(
        "Validates '%s' as a driver class.",
        FacadeException::new,
        driverClass,
        Objects::nonNull,
        aClass -> driverClass.isAnnotationPresent(RunWith.class),
        (Predicate<Class<?>>) aClass -> driverClass.getAnnotation(RunWith.class).value().equals(ScriptiveUnit.class),
        (Predicate<Class<?>>) FacadeException::hasPublicConstructor,
        (Predicate<Class<?>>) FacadeException::constructorHasNoParameter
    );
  }

  public static Class<?> validateSuiteSetClass(Class<?> suiteSetClass) {
    return validate(
        "Validates '%s' as a suite set class.",
        FacadeException::new,
        suiteSetClass,
        prettify("is non-null", (Predicate<?>) Objects::nonNull),
        prettify("has @RunWith", (Class<?> aClass) -> suiteSetClass.isAnnotationPresent(RunWith.class)),
        prettify("has ScriptiveSuiteSet.class as value of @RunWith", (Class<?> aClass) -> suiteSetClass.getAnnotation(RunWith.class).value().equals(ScriptiveSuiteSet.class)),
        prettify("has @SuiteScripts", (Class<?> aClass) -> aClass.isAnnotationPresent(SuiteScripts.class)),
        prettify("has one and only one public constructor", FacadeException::hasPublicConstructor),
        prettify("only one constructor has no parameter", FacadeException::constructorHasNoParameter)
    );
  }


  private static boolean hasPublicConstructor(Class<?> driverClass) {
    return driverClass.getConstructors().length == 1;
  }

  private static boolean constructorHasNoParameter(Class<?> driverClass) {
    return driverClass.getConstructors()[0].getParameterTypes().length == 0;
  }
}
