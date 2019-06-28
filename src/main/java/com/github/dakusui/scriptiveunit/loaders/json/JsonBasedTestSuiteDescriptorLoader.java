package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.session.Session;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class JsonBasedTestSuiteDescriptorLoader extends TestSuiteDescriptorLoader.Base<JsonNode, ObjectNode, ArrayNode, JsonNode> {

  @SuppressWarnings("unused")
  public JsonBasedTestSuiteDescriptorLoader(Config config) {
    super(config);
  }

  // TEMPLATE
  @Override
  public TestSuiteDescriptor loadTestSuiteDescriptor(Session session) {
    return hostSpec.mapObjectNode(
        hostSpec.translate(readScriptHandlingInheritance(session.getConfig().getScriptResourceName())),
        JsonTestSuiteDescriptorBean.class)
        .create(session);
  }

  @Override
  protected ApplicationSpec modelSpec() {
    return new ApplicationSpec.Standard();
  }

  @Override
  protected HostSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> hostLanguage() {
    return new HostSpec.Json();
  }
}
