package com.github.dakusui.scriptiveunit.unittests.cli;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;

import java.util.function.Function;
import java.util.function.Predicate;

public class CliException extends ScriptiveUnitException {
  private CliException(String message) {
    super(message, null);
  }

  private static <T> T check(T value, Predicate<T> conditon, Function<T, String> format) {
    if (conditon.test(value))
      return value;
    throw new CliException(format.apply(value));
  }
}
