package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.loaders.preprocessing.PreprocessingUnit;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.lang.ResourceStoreSpec;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import com.github.dakusui.scriptiveunit.utils.JsonUtils.NodeFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.printables.Printables.isEqualTo;
import static com.github.dakusui.scriptiveunit.utils.IoUtils.currentWorkingDirectory;
import static java.util.Collections.emptyList;

@RunWith(Enclosed.class)
public class PreprocessorTest {
  private static ObjectNode objectNode(NodeFactory<ObjectNode> nodeFactory) {
    return nodeFactory.get();
  }

  abstract static class Base extends TestBase {
    @Test
    public void test() {
      then(when(given(), createApplicationSpec(), createHostSpec(), createResourceStoreSpec(createAdditionalResources())));
    }

    public abstract ApplicationSpec.Dictionary given();

    public ApplicationSpec.Dictionary when(
        ApplicationSpec.Dictionary input,
        ApplicationSpec applicationSpec,
        HostSpec<?, ?, ?, ?> hostSpec,
        ResourceStoreSpec resourceStoreSpec) {
      return Preprocessor.create(hostSpec, applicationSpec).preprocess(input, resourceStoreSpec);
    }

    public void then(ApplicationSpec.Dictionary preprocessedDictionary) {
      ////
      // Convert a dictionary to an object node for the sake of verification handiness.
      ObjectNode preprocessedObjectNode = toObjectNode(preprocessedDictionary);
      System.out.println("preprocessedDictionary:<" + preprocessedObjectNode + ">");
      verifyObjectNode(preprocessedObjectNode);
    }

    abstract void verifyObjectNode(ObjectNode preprocessedObjectNode);

    ApplicationSpec.Dictionary createDefaultValues() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict();
        }
      }.create();
    }

    Map<String, ObjectNode> createAdditionalResources() {
      return Collections.emptyMap();
    }

    ApplicationSpec createApplicationSpec() {
      return new ApplicationSpec.Base() {
        @Override
        public Dictionary createDefaultValues() {
          return PreprocessorTest.Base.this.createDefaultValues();
        }

        @Override
        public List<PreprocessingUnit> preprocessorUnits() {
          return emptyList();
        }
      };
    }

    HostSpec<?, ?, ?, ?> createHostSpec() {
      return new HostSpec.Json();
    }

    ResourceStoreSpec createResourceStoreSpec(Map<String, ObjectNode> additionalResources) {
      return new ResourceStoreSpec.Impl(currentWorkingDirectory()) {
        @Override
        public ObjectNode readObjectNode(String resourceName) {
          if (additionalResources.containsKey(resourceName))
            return additionalResources.get(resourceName);
          return super.readObjectNode(resourceName);
        }
      };
    }

    private static ObjectNode toObjectNode(ApplicationSpec.Dictionary dictionary) {
      return new HostSpec.Json().toHostObject(dictionary);
    }
  }

  public static class Simple extends Base {
    @Override
    public ApplicationSpec.Dictionary given() {
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
    public ApplicationSpec.Dictionary given() {
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
    public ApplicationSpec.Dictionary given() {
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
    public ApplicationSpec.Dictionary given() {
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

  public static class DefaultValues extends Base {
    @Override
    public ApplicationSpec.Dictionary given() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict(
              $("defined", "definedValue")
          );
        }
      }.create();
    }

    @Override
    public ApplicationSpec.Dictionary createDefaultValues() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict(
              $("default", "defaultValue"),
              $("defaultArray", array("defaultArray1", "defaultArray2")),
              $("defaultDict", dict(
                  $("defaultDictKey1", "defaultDictValue1"),
                  $("defaultDictKey2", "defaultDictValue2")))
          );
        }
      }.create();
    }

    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {
      assertThat(
          preprocessedObjectNode,
          allOf(
              asString(call("get", "default").andThen("asText").$()).equalTo("defaultValue").$(),
              asString(call("get", "defined").andThen("asText").$()).equalTo("definedValue").$(),
              asObject(call("get", "defaultArray").$())
                  .check(call("get", 0).andThen("asText").$(), isEqualTo("defaultArray1"))
                  .check(call("get", 1).andThen("asText").$(), isEqualTo("defaultArray2"))
                  .$(),
              asObject(call("get", "defaultDict").$())
                  .check(call("get", "defaultDictKey1").andThen("asText").$(), isEqualTo("defaultDictValue1"))
                  .check(call("get", "defaultDictKey2").andThen("asText").$(), isEqualTo("defaultDictValue2"))
                  .$()
          ));
    }
  }

  public static class Inheritance extends Base {
    @Override
    public ApplicationSpec.Dictionary given() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict($("$extends", array("parent")));
        }
      }.create();
    }

    Map<String, ObjectNode> createAdditionalResources() {
      return new HashMap<String, ObjectNode>() {{
        put("parent", new NodeFactory<ObjectNode>() {
          @Override
          public JsonNode create() {
            return obj($("key", $("valueInParent")));
          }
        }.get());
      }};
    }

    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {
      assertThat(
          preprocessedObjectNode,
          asString(call("get", "key").andThen("asText").$()).equalTo("valueInParent").$()
      );
    }
  }

  public static class Overriding extends Base {
    @Override
    public ApplicationSpec.Dictionary given() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict(
              $("$extends", array("parent")),
              $("keyBothInParentAndChild", "valueInChild"),
              $("keyOnlyInChild", "valueOnlyInChild")
          );
        }
      }.create();
    }

    Map<String, ObjectNode> createAdditionalResources() {
      return new HashMap<String, ObjectNode>() {{
        put("parent", new NodeFactory<ObjectNode>() {
          @Override
          public JsonNode create() {
            return obj(
                $("keyBothInParentAndChild", $("valueInParent")),
                $("keyOnlyInParent", $("valueOnlyInParent"))
            );
          }
        }.get());
      }};
    }

    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {
      assertThat(
          preprocessedObjectNode,
          allOf(
              asString(call("get", "keyBothInParentAndChild").andThen("asText").$()).equalTo("valueInChild").$(),
              asString(call("get", "keyOnlyInChild").andThen("asText").$()).equalTo("valueOnlyInChild").$(),
              asString(call("get", "keyOnlyInParent").andThen("asText").$()).equalTo("valueOnlyInParent").$()
          ));
    }
  }

  public static class OverridingDefault extends Base {
    @Override
    public ApplicationSpec.Dictionary given() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict(
              $("$extends", array("parent")),
              $("key", "valueInChild"),
              $("keyOnlyInChild", "valueOnlyInChild"),
              $("keyInDefaultOverriddenByChild", "valueInChildOverridingDefault")
          );
        }
      }.create();
    }

    @Override
    public ApplicationSpec.Dictionary createDefaultValues() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict(
              $("keyOnlyInDefault", "valueOnlyInDefault"),
              $("keyInDefaultOverriddenByChild", "valueInDefaultOverriddenByChild"),
              $("keyInDefaultOverriddenByParent", "valueInDefaultOverriddenByParent")
          );
        }
      }.create();
    }


    Map<String, ObjectNode> createAdditionalResources() {
      return new HashMap<String, ObjectNode>() {{
        put("parent", new NodeFactory<ObjectNode>() {
          @Override
          public JsonNode create() {
            return obj(
                $("key", $("valueInParent")),
                $("keyOnlyInParent", $("valueOnlyInParent")),
                $("keyInDefaultOverriddenByParent", $("valueInParentOverridingDefault"))
            );
          }
        }.get());
      }};
    }

    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {
      assertThat(
          preprocessedObjectNode,
          allOf(
              asString(call("get", "key").andThen("asText").$()).equalTo("valueInChild").$(),
              asString(call("get", "keyOnlyInChild").andThen("asText").$())
                  .equalTo("valueOnlyInChild").$(),
              asString(call("get", "keyOnlyInParent").andThen("asText").$())
                  .equalTo("valueOnlyInParent").$(),
              asString(call("get", "keyInDefaultOverriddenByChild").andThen("asText").$())
                  .equalTo("valueInChildOverridingDefault").$(),
              asString(call("get", "keyInDefaultOverriddenByParent").andThen("asText").$()).equalTo("valueInParentOverridingDefault").$()

          ));
    }
  }

  public static class NestedInheritance extends Base {
    @Override
    public ApplicationSpec.Dictionary given() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict(
              $("$extends", array("parent"))
          );
        }
      }.create();
    }

    Map<String, ObjectNode> createAdditionalResources() {
      return new HashMap<String, ObjectNode>() {
        {
          put("parent", objectNode(new NodeFactory<ObjectNode>() {
            @Override
            public JsonNode create() {
              return obj($("$extends", arr("grandParent")));
            }
          }));
          put("grandParent", objectNode(new NodeFactory<ObjectNode>() {
            @Override
            public JsonNode create() {
              return obj($("key", $("valueInGrandParent")));
            }
          }));
        }

      };
    }

    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {
      assertThat(
          preprocessedObjectNode,
          asString(call("get", "key").andThen("asText").$()).equalTo("valueInGrandParent").$()
      );
    }
  }

  public static class NestedOverriding extends Base {
    @Override
    public ApplicationSpec.Dictionary given() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict(
              $("$extends", array("parent"))
          );
        }
      }.create();
    }

    Map<String, ObjectNode> createAdditionalResources() {
      return new HashMap<String, ObjectNode>() {
        {
          put("parent", objectNode(new NodeFactory<ObjectNode>() {
            @Override
            public JsonNode create() {
              return obj(
                  $("$extends", arr("grandParent")),
                  $("key", $("valueInParent")));
            }
          }));
          put("grandParent", objectNode(new NodeFactory<ObjectNode>() {
            @Override
            public JsonNode create() {
              return obj($("key", $("valueInGrandParent")));
            }
          }));
        }
      };
    }

    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {
      assertThat(
          preprocessedObjectNode,
          asString(call("get", "key").andThen("asText").$()).equalTo("valueInParent").$()
      );
    }
  }

  public static class DoubleNestedOverriding extends Base {
    @Override
    public ApplicationSpec.Dictionary given() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict(
              $("$extends", array("parent"))
          );
        }
      }.create();
    }

    Map<String, ObjectNode> createAdditionalResources() {
      return new HashMap<String, ObjectNode>() {
        {
          put("parent", objectNode(new NodeFactory<ObjectNode>() {
            @Override
            public JsonNode create() {
              return obj($("$extends", arr("grandParent")));
            }
          }));
          put("grandParent", objectNode(new NodeFactory<ObjectNode>() {
            @Override
            public JsonNode create() {
              return obj($("$extends", arr("grandGrandParent")),
                  $("key", $("valueInGrandParent")));
            }
          }));
          put("grandGrandParent", objectNode(new NodeFactory<ObjectNode>() {
            @Override
            public JsonNode create() {
              return obj($("key", $("valueInGrandGrandParent")));
            }
          }));
        }
      };
    }

    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {
      assertThat(
          preprocessedObjectNode,
          asString(call("get", "key").andThen("asText").$()).equalTo("valueInGrandParent").$()
      );
    }
  }

  public static class MultipleInheritance extends Base {
    @Override
    public ApplicationSpec.Dictionary given() {
      return new ApplicationSpec.Dictionary.Factory() {
        ApplicationSpec.Dictionary create() {
          return dict($("$extends", array("parent1", "parent2")));
        }
      }.create();
    }

    Map<String, ObjectNode> createAdditionalResources() {
      return new HashMap<String, ObjectNode>() {
        {
          put("parent1", objectNode(new NodeFactory<ObjectNode>() {
            @Override
            public JsonNode create() {
              return obj(
                  $("keyOnlyInParent1", $("valueOnlyInParent1")),
                  $("keyBothInParent1AndParent2", $("valueInParent1"))
              );
            }
          }));
          put("parent2", objectNode(new NodeFactory<ObjectNode>() {
            @Override
            public JsonNode create() {
              return obj($("keyOnlyInParent2", $("valueOnlyInParent2")),
                  $("keyBothInParent1AndParent2", $("valueInParent2")));
            }
          }));
          put("grandGrandParent", objectNode(new NodeFactory<ObjectNode>() {
            @Override
            public JsonNode create() {
              return obj($("key", $("valueInGrandGrandParent")));
            }
          }));
        }
      };
    }


    @Override
    void verifyObjectNode(ObjectNode preprocessedObjectNode) {
      assertThat(
          preprocessedObjectNode,
          allOf(
              asString(call("get", "keyOnlyInParent1").andThen("asText").$())
                  .equalTo("valueOnlyInParent1").$(),
              asString(call("get", "keyOnlyInParent2").andThen("asText").$())
                  .equalTo("valueOnlyInParent2").$(),
              asString(call("get", "keyBothInParent1AndParent2").andThen("asText").$())
                  .equalTo("valueInParent1").$()
          ));
    }
  }

}
