package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.loaders.json.TestSuiteDescriptorBeanFromJson;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.lang.LanguageSpec;
import com.github.dakusui.scriptiveunit.model.session.Session;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapIfNecessary;

public interface ScriptCompiler {

  TestSuiteDescriptor compile(Session session);

  class Default implements ScriptCompiler {
    @Override
    public TestSuiteDescriptor compile(Session session) {
      return mapObjectNodeToJsonTestSuiteDescriptorBean(getRootNode(session.getScript().languageSpec())).create(session);
    }

    static ObjectNode getRootNode(LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec) {
      return new HostSpec.Json().toHostObject(JsonScript.processScript(languageSpec));
    }

    protected static TestSuiteDescriptorBeanFromJson mapObjectNodeToJsonTestSuiteDescriptorBean(ObjectNode rootNode) {
      try {
        return new ObjectMapper().readValue(
            rootNode,
            TestSuiteDescriptorBeanFromJson.class);
      } catch (IOException e) {
        throw wrapIfNecessary(e);
      }
    }
  }
}
