package com.github.dakusui.scriptiveunit.model.lang.json;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public enum JsonUtils {
  ;

  public static JsonNode readJsonNodeFromStream(InputStream is) {
    try {
      return new ObjectMapper().readTree(is);
    } catch (IOException e) {
      throw ScriptiveUnitException.wrap(e, "Non-welformed input is given.");
    }
  }
}
