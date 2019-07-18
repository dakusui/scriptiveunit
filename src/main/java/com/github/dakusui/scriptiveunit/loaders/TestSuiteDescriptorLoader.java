package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.json.JsonTestSuiteDescriptorBean;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.HostSpec;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.session.Session;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface TestSuiteDescriptorLoader {
  static TestSuiteDescriptorLoader createTestSuiteDescriptorLoader(
      Class<? extends TestSuiteDescriptorLoader> loaderClass,
      Script script) {
    return createInstance(loaderClass, script);
  }

  Script getScript();

  TestSuiteDescriptor loadTestSuiteDescriptor(Session session);

  class Impl implements TestSuiteDescriptorLoader {
    private final Script script;

    public Impl(Script script) {
      this.script = script;
    }

    public Script getScript() {
      return this.script;
    }

    @Override
    public TestSuiteDescriptor loadTestSuiteDescriptor(Session session) {
      return mapObjectNodeToJsonTestSuiteDescriptorBean(
          new HostSpec.Json()
              .toHostObject(getScript().readScriptResource()))
          .create(session);
    }

    static private JsonTestSuiteDescriptorBean mapObjectNodeToJsonTestSuiteDescriptorBean(ObjectNode rootNode) {
      try {
        return new ObjectMapper().readValue(
            rootNode,
            JsonTestSuiteDescriptorBean.class);
      } catch (IOException e) {
        throw ScriptiveUnitException.wrapIfNecessary(e);
      }
    }
  }

  static TestSuiteDescriptorLoader createInstance(Class<? extends TestSuiteDescriptorLoader> klass, Script script) {
    try {
      return klass.getConstructor(Script.class).newInstance(script);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw ScriptiveUnitException.wrapIfNecessary(e);
    }
  }
}
