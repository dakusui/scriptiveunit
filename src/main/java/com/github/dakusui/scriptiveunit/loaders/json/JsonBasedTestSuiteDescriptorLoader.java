package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.session.Session;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;

public class JsonBasedTestSuiteDescriptorLoader extends TestSuiteDescriptorLoader.Base<JsonNode, ObjectNode, ArrayNode> {

  @SuppressWarnings("unused")
  public JsonBasedTestSuiteDescriptorLoader(Config config) {
    super(config);
  }

  // TEMPLATE
  @Override
  public TestSuiteDescriptor loadTestSuiteDescriptor(Session session) {
    return mapObjectNodeToJsonTestSuiteDescriptorBean(
        new HostSpec.Json().toHostObject(readScriptHandlingInheritance(session.getConfig().getScriptResourceName()))
    )
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

  private JsonTestSuiteDescriptorBean mapObjectNodeToJsonTestSuiteDescriptorBean(ObjectNode rootNode) {
    try {
      return new ObjectMapper().readValue(
          rootNode,
          JsonTestSuiteDescriptorBean.class);
    } catch (IOException e) {
      throw wrap(e);
    }
  }
}
