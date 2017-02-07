package com.github.dakusui.scriptunit.exceptions;

import java.util.function.Supplier;

import static java.lang.String.format;

public class SyntaxException extends ScriptUnitException {
  private SyntaxException(String message) {
    super(message);
  }

  public static Supplier<SyntaxException> attributeNotFound(String attributeName, Object context, Iterable<String> knownAttributeNames) {
    return () -> {
      throw new SyntaxException(format(
          "Attribute '%s' is accessed in '%s', but not found in your test case. Known attribute names are %s'",
          attributeName,
          context,
          knownAttributeNames));
    };
  }

  public static SyntaxException typeMismatch(Class type, Object input) {
    throw new SyntaxException(format("Type mismatch. '%s'(%s) couldn't be converted to %s",
        input,
        input != null ? input.getClass().getCanonicalName() : "none",
        type.getCanonicalName()
    ));
  }
}
