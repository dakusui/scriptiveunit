package com.github.dakusui.scriptiveunit.tests.preprocessing;

import com.github.dakusui.scriptiveunit.core.Preprocessor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class PreprocessorTest {
  @Test
  public void whenPreprocessingOnArrayIsRequested() throws IOException {
    JsonNode targetObject = new ObjectMapper().readTree("{\"a1\":[0,1,2]}");
    Preprocessor preprocessor = new Preprocessor() {
      @Override
      public JsonNode translate(JsonNode targetElement) {
        ObjectNode ret = new ObjectNode(JsonNodeFactory.instance);
        ret.put("v1", "Hello");
        ret.put("v2", targetElement);
        return ret;
      }

      @Override
      public boolean matches(Path pathToTargetElement) {
        return Utils.pathComponentList("a1").equals(
            pathToTargetElement.asComponentList()
        );
      }
    };
    assertEquals(
        "{\"a1\":{\"v1\":\"Hello\",\"v2\":[0,1,2]}}",
        Preprocessor.translate(preprocessor, targetObject).toString()
    );
  }

  @Test
  public void whenPreprocessingOnMapIsRequested() throws IOException {
    JsonNode targetObject = new ObjectMapper().readTree("{\"a1\":{\"c1\":100, \"c2\":200}}");
    Preprocessor preprocessor = new Preprocessor() {
      @Override
      public JsonNode translate(JsonNode targetElement) {
        ObjectNode ret = new ObjectNode(JsonNodeFactory.instance);
        ret.put("v1", "Hello");
        ret.put("v2", targetElement);
        return ret;
      }

      @Override
      public boolean matches(Path pathToTargetElement) {
        return Utils.pathComponentList("a1", "c2").equals(
            pathToTargetElement.asComponentList()
        );
      }
    };
    assertEquals(
        "{\"a1\":{\"c1\":100,\"c2\":{\"v1\":\"Hello\",\"v2\":200}}}",
        Preprocessor.translate(preprocessor, targetObject).toString()
    );
  }

}
