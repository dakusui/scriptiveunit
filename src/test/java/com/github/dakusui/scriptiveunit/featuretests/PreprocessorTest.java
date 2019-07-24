package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

public class PreprocessorTest {
  @Test
  public void test() {
    ObjectNode rawObjectNode = toObjectNode(createDictionary());
    ApplicationSpec applicationSpec = createApplicationSpec();
    HostSpec.Json hostSpec = createHostSpec();
    ObjectNode preprocessedObjectNode = preprocess(rawObjectNode, applicationSpec, hostSpec);
    System.out.println(preprocessedObjectNode);
  }

  private ApplicationSpec.Standard createApplicationSpec() {
    return new ApplicationSpec.Standard();
  }

  private HostSpec.Json createHostSpec() {
    return new HostSpec.Json();
  }

  private ApplicationSpec.Dictionary createDictionary() {
    return new ApplicationSpec.Dictionary.Factory() {
      ApplicationSpec.Dictionary create() {
        return dict($("hello", "world"));
      }
    }.create();
  }


  private static ObjectNode preprocess(ObjectNode objectNode, ApplicationSpec applicationSpec, HostSpec.Json hostSpec) {
    return toObjectNode(createPreprocessor(hostSpec, applicationSpec)
        .preprocess(hostSpec.toApplicationDictionary(objectNode)));
  }

  private static Preprocessor createPreprocessor(HostSpec.Json hostSpec, ApplicationSpec applicationSpec) {
    return new Preprocessor.Builder(hostSpec)
        .applicationSpec(applicationSpec)
        .build();
  }

  private static ObjectNode toObjectNode(ApplicationSpec.Dictionary dictionary) {
    return new HostSpec.Json().toHostObject(dictionary);
  }
}
