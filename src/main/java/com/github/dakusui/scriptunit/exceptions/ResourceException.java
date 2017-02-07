package com.github.dakusui.scriptunit.exceptions;

import com.github.dakusui.scriptunit.core.SystemProperty;

import static java.lang.String.format;

public class ResourceException extends ScriptUnitException {
  public ResourceException(String message) {
    super(message);
  }

  public static ResourceException scriptNotFound(String scriptName) {
    throw new ResourceException(format("Script '%s' was not found. Check your classpath.", scriptName));
  }

  public static ResourceException scriptNotSpecified() {
    return new ResourceException(format("Script to be run was not specified. Use -D%s={FQCN of your script} system property", SystemProperty.TARGET.getKey()));
  }
}
