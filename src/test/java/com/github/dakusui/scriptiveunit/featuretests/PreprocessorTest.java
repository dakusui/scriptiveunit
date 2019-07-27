package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.loaders.preprocessing.PreprocessingUnit;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.lang.ResourceStoreSpec;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.dakusui.crest.Crest.*;
import static java.util.Collections.emptyList;

@RunWith(Enclosed.class)
public class PreprocessorTest {
  abstract static class Base extends TestBase {
    @Test
    public void test() {
      then(when(createApplicationSpec(), createHostSpec(), given()));
    }

    public void then(ObjectNode preprocessedObjectNode) {
      System.out.println("preprocessedNode=<" + preprocessedObjectNode + ">");
      verifyObjectNode(preprocessedObjectNode);
    }

    abstract void verifyObjectNode(ObjectNode preprocessedObjectNode);

    public ObjectNode when(ApplicationSpec applicationSpec, HostSpec.Json hostSpec, ObjectNode rawObjectNode) {
      return preprocess(rawObjectNode, applicationSpec, hostSpec);
    }

    public ObjectNode given() {
      return toObjectNode(createDictionary());
    }


    private ApplicationSpec createApplicationSpec() {
      return new ApplicationSpec.Base() {
        @Override
        public Dictionary createDefaultValues() {
          return createDictionary();
        }

        @Override
        public List<PreprocessingUnit> preprocessors() {
          return emptyList();
        }
      };
    }

    private HostSpec.Json createHostSpec() {
      return new HostSpec.Json();
    }

    abstract ApplicationSpec.Dictionary createDictionary();

    private static ObjectNode preprocess(ObjectNode objectNode, ApplicationSpec applicationSpec, HostSpec.Json hostSpec) {
      return toObjectNode(createPreprocessor(hostSpec, applicationSpec).preprocess(hostSpec.toApplicationDictionary(objectNode), new ResourceStoreSpec.Impl()));
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

  public static class Simple extends Base {
    @Override
    ApplicationSpec.Dictionary createDictionary() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict($("hello", "world"));
        }
      }.create();
    }

    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {

    }
  }

  public static class Types extends Base {
    @Override
    ApplicationSpec.Dictionary createDictionary() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict(
              $("stringValue", "hello"),
              $("booleanValue", true),
              $("intValue", 123),
              $("longValue", 1234),
              $("shortValue", 12),
              $("floatValue", 123.0),
              $("doubleValue", 1234.0)
          );
        }
      }.create();
    }

    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {
      assertThat(
          preprocessedObjectNode,
          allOf(
              asString(call("get", "stringValue").andThen("asText").$()).equalTo("hello").$(),
              asBoolean(call("get", "booleanValue").andThen("asBoolean").$()).equalTo(Boolean.TRUE).$()
          ));
    }
  }

  public static class NanValues extends Base {
    @Override
    ApplicationSpec.Dictionary createDictionary() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict(
              $("floatNaN", Float.NaN),
              $("doubleNaN", Double.NaN)
          );
        }
      }.create();
    }

    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {
      assertThat(
          preprocessedObjectNode,
          allOf(
              asString(call("get", "floatNaN").andThen("asDouble").$()).check("isNaN").$(),
              asBoolean(call("get", "doubleNaN").andThen("asDouble").$()).check("isNaN").$()
          ));

    }
  }

  public static class Null extends Base {
    @Override
    ApplicationSpec.Dictionary createDictionary() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict(
              $("nullValue", null)
          );
        }
      }.create();
    }

    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {
      assertThat(
          preprocessedObjectNode,
          asObject("get", "nullValue").check("isNull").$()
      );
    }
  }
}
