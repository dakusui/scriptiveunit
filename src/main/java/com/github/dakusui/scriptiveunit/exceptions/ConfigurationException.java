package com.github.dakusui.scriptiveunit.exceptions;

import com.github.dakusui.scriptiveunit.GroupedTestItemRunner;

import static java.lang.String.format;

public class ConfigurationException extends ScriptiveUnitException {
  public ConfigurationException(String message) {
    super(message);
  }

  public static ConfigurationException unsupportedRunMode(GroupedTestItemRunner.Type runnerType) {
    throw new ConfigurationException(format("Runner type '%s' is not supported", runnerType));
  }

  public static  ConfigurationException scriptNotSpecified(String scriptSystemPropertyKey) {
    throw new ConfigurationException(
        format("Script to be run was not specified. Give -D%s={FQCN of your script} to your command line as a VM option.",
            scriptSystemPropertyKey)
    );
  }
}
