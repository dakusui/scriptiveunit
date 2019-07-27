package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.json.TestSuiteDescriptorBeanFromJson;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.lang.ResourceStoreSpec;
import com.github.dakusui.scriptiveunit.model.session.Session;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;

public interface ScriptCompiler {

  TestSuiteDescriptor compile(Session session, ResourceStoreSpec resourceStoreSpec);

  class Default implements ScriptCompiler {
    @Override
    public TestSuiteDescriptor compile(Session session, ResourceStoreSpec resourceStoreSpec) {
      return mapObjectNodeToJsonTestSuiteDescriptorBean(getRootNode(session.getScript())).create(session);
    }

    static ObjectNode getRootNode(Script<JsonNode, ObjectNode, ArrayNode, JsonNode> script) {
      return new HostSpec.Json().toHostObject(
          JsonScript.processScript(script.languageSpec(), script.mainNode()));
    }

    static TestSuiteDescriptorBeanFromJson mapObjectNodeToJsonTestSuiteDescriptorBean(ObjectNode rootNode) {
      try {
        return new ObjectMapper().readValue(
            rootNode,
            TestSuiteDescriptorBeanFromJson.class);
      } catch (IOException e) {
        throw ScriptiveUnitException.wrapIfNecessary(e);
      }
    }
  }
}
