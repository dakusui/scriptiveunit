package com.github.dakusui.scriptunit.exceptions;

import com.github.dakusui.scriptunit.ScriptRunner;

import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class ConfigurationException extends ScriptUnitException {
  public ConfigurationException(String message) {
    super(message);
  }

  public static ConfigurationException unsupportedRunMode(ScriptRunner.Type runnerType) {
    throw new ConfigurationException(format("Runner type '%s' is not supported", runnerType));
  }

  public static ConfigurationException helpRequested() {
    throw new ConfigurationException("Help is printed. Bye.");
  }

  public static <E extends ScriptUnitException> Supplier<E> invalidSystemPropertyValue(String key, Pattern regex, String value) {
    return () -> {
      throw new ConfigurationException(format("System property value '%s' given to '%s' did not match '%s'", value, key, regex));
    };
  }
}
