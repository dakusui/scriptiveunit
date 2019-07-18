package com.github.dakusui.scriptiveunit.exceptions;

import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.model.form.Form;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class ConfigurationException extends ScriptiveUnitException {
  private ConfigurationException(String message) {
    super(message);
  }

  public static ConfigurationException scriptNotSpecified(JsonScript script) {
    if (script instanceof JsonScript.Standard) {
      String key = ((JsonScript.Standard) script).getScriptResourceNameKey()
          .orElseThrow(ScriptiveUnitException::noScriptResourceNameKeyWasGiven);
      throw new ConfigurationException(format(
          "Script to be run was not specified. Give -D%s={FQCN of your script} to your command line as a VM option.",
          key));
    } else {
      throw noScriptResourceWasGiven();
    }
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

  private static ScriptiveUnitException noScriptResourceWasGiven() {
    throw new ScriptiveUnitException("No script was given in this session.");
  }

  public static ScriptiveUnitException nonStandardScript(Script script) {
    throw new ScriptiveUnitException(format("Non-standard config:<%s> was given", script));
  }
}
