package com.github.dakusui.scriptiveunit.exceptions;

import com.github.dakusui.scriptiveunit.model.stage.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import org.codehaus.jackson.JsonNode;

import java.util.Map;
import java.util.function.Supplier;

import static java.lang.String.format;

public class SyntaxException extends ScriptiveUnitException {
  public SyntaxException(String message) {
    super(message);
  }

}
