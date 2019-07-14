package com.github.dakusui.scriptiveunit.exceptions;

import com.github.dakusui.scriptiveunit.model.form.Form;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class ConfigurationException extends ScriptiveUnitException {
  private ConfigurationException(String message) {
    super(message);
  }

  public static ConfigurationException scriptNotSpecified(String scriptSystemPropertyKey) {
    throw new ConfigurationException(
        format("Script to be run was not specified. Give -D%s={FQCN of your script} to your command line as a VM option.",
            scriptSystemPropertyKey)
    );
  }

  public static ConfigurationException duplicatedFormsAreFound(Map<String, List<Form>> duplicatedObjectMethods) {
    StringBuffer buf = new StringBuffer();
    duplicatedObjectMethods.forEach((s, objectMethods) -> {
      buf.append(format("%s:%n", s));
      objectMethods.forEach(each -> buf.append(format("  %s%n", each)));
      buf.append(format("%n"));
    });
    String found = buf.toString();
    throw new ConfigurationException(format(
        "Following object methods are found duplicated:%n%s", found
    ));
  }
}
