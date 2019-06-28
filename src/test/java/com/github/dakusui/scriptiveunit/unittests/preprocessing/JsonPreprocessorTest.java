package com.github.dakusui.scriptiveunit.unittests.preprocessing;

import com.github.dakusui.scriptiveunit.loaders.Preprocessor;
import com.github.dakusui.scriptiveunit.loaders.json.HostLanguage;
import com.github.dakusui.scriptiveunit.loaders.json.ModelSpec;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JsonPreprocessorTest {
  @Test
  public void whenPreprocessingOnArrayIsRequested() throws IOException {
    ObjectNode targetObject = (ObjectNode) new ObjectMapper().readTree("{\"a1\":[0,1,2]}");
    Preprocessor jsonPreprocessor = new Preprocessor() {
      @Override
      public ModelSpec.Node translate(ModelSpec.Node targetElement) {
        return ModelSpec.dict(
            ModelSpec.$("v1", ModelSpec.atom("Hello")),
            ModelSpec.$("v2", targetElement)
        );
      }

      @Override
      public boolean matches(Path pathToTargetElement) {
        return Preprocessor.Utils.pathComponentList("a1").equals(
            pathToTargetElement.asComponentList()
        );
      }
    };
    HostLanguage.Json hostLanguage = new HostLanguage.Json();
    assertEquals(
        "{\"a1\":{\"v1\":\"Hello\",\"v2\":[0,1,2]}}",
        hostLanguage.translate(ModelSpec.preprocess(hostLanguage.toModelDictionary(targetObject), jsonPreprocessor)).toString()
    );
  }

  @Test
  public void whenPreprocessingOnMapIsRequested() throws IOException {
    ObjectNode targetObject = (ObjectNode) new ObjectMapper().readTree("{\"a1\":{\"c1\":100, \"c2\":200}}");
    Preprocessor jsonPreprocessor = new Preprocessor() {
      @Override
      public ModelSpec.Node translate(ModelSpec.Node targetElement) {
        return ModelSpec.dict(
            ModelSpec.$("v1", ModelSpec.atom("Hello")),
            ModelSpec.$("v2", targetElement)
        );
      }

      @Override
      public boolean matches(Path pathToTargetElement) {
        return Preprocessor.Utils.pathComponentList("a1", "c2").equals(
            pathToTargetElement.asComponentList()
        );
      }
    };
    HostLanguage.Json hostLanguage = new HostLanguage.Json();
    assertEquals(
        "{\"a1\":{\"c1\":100,\"c2\":{\"v1\":\"Hello\",\"v2\":200}}}",
        hostLanguage.translate(ModelSpec.preprocess(hostLanguage.toModelDictionary(targetObject), jsonPreprocessor)).toString()
    );
  }

}
