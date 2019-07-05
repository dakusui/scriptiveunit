package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class JsonBasedTestSuiteDescriptorLoader extends TestSuiteDescriptorLoader.ScriptBased<JsonNode, ObjectNode, ArrayNode> {

  @SuppressWarnings("unused")
  public JsonBasedTestSuiteDescriptorLoader(Config config) {
    super(config);
  }

  @Override
  protected ApplicationSpec createApplicationSpec() {
    return new ApplicationSpec.Standard();
  }

  @Override
  protected HostSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> createHostSpec() {
    return new HostSpec.Json();
  }
}
