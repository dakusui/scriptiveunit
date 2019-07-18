package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.scriptiveunit.core.JsonScript;
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

public interface ScriptCompiler {
  TestSuiteDescriptor compile(Session session, Script script);

  class Impl implements ScriptCompiler {
    private final JsonScript script;

    public Impl(JsonScript script) {
      this.script = script;
    }

    @SuppressWarnings("JavaReflectionInvocation")
    public static ScriptCompiler createInstance(Class<? extends ScriptCompiler.Impl> klass, Script script) {
      try {
        return klass.getConstructor(JsonScript.class).newInstance(script);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw ScriptiveUnitException.wrapIfNecessary(e);
      }
    }

    public JsonScript getScript() {
      return this.script;
    }

    @Override
    public TestSuiteDescriptor compile(Session session, Script script) {
      return mapObjectNodeToJsonTestSuiteDescriptorBean(
          new HostSpec.Json()
              .toHostObject(script.readScriptResource()))
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

}
