package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;

import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.nonObject;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;

public enum JsonUtils {
  ;

  public static JsonNode readJsonNodeFromStream(InputStream is) {
    try {
      return new ObjectMapper().readTree(is);
    } catch (IOException e) {
      throw ScriptiveUnitException.wrap(e, "Non-welformed input is given.");
    }
  }

  public static ObjectNode requireObjectNode(JsonNode curr) {
    return (ObjectNode) check(curr, v -> curr.isObject(), () -> nonObject(curr));
  }
}
