package com.github.dakusui.scriptiveunit.exceptions;

import static java.lang.String.format;

public class ResourceException extends ScriptiveUnitException {
  private ResourceException(String message) {
    super(message);
  }

  public static <T> T scriptExists(T target, String scriptName) {
    if (target == null)
      throw new ResourceException(format("Script '%s' was not found. Check your classpath.", scriptName));
    return target;
  }

  public static ResourceException functionNotFound(String functionName) {
    throw new ResourceException(String.format("A function '%s' was not found.", functionName));
  }

}
