package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.json.JsonTestSuiteDescriptorBean;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.HostSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.session.Session;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static com.github.dakusui.scriptiveunit.exceptions.ConfigurationException.scriptNotSpecified;

public interface TestSuiteDescriptorLoader {
  static TestSuiteDescriptorLoader createTestSuiteDescriptorLoader(
      Class<? extends TestSuiteDescriptorLoader> loaderClass,
      Config config) {
    return createInstance(loaderClass, config);
  }

  Config getConfig();

  TestSuiteDescriptor loadTestSuiteDescriptor(Session session);

  abstract class Base<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE>
      implements TestSuiteDescriptorLoader {
    private final Preprocessor preprocessor = new Preprocessor.Builder<>(createHostSpec())
        .applicationSpec(createApplicationSpec())
        .rawScriptReader(this::readRawScriptResource)
        .build();

    private final Config config;

    protected Base(Config config) {
      this.config = config;
    }

    public Config getConfig() {
      return this.config;
    }

    @Override
    public TestSuiteDescriptor loadTestSuiteDescriptor(Session session) {
      return mapObjectNodeToJsonTestSuiteDescriptorBean(
          new HostSpec.Json().toHostObject(
              preprocessor.readScript(
                  session.getConfig()
                      .getScriptResourceName()
                      .orElseThrow(() -> scriptNotSpecified(session.getConfig())))))
          .create(session);
    }

    abstract protected ApplicationSpec createApplicationSpec();

    abstract protected HostSpec<NODE, OBJECT, ARRAY, ATOM> createHostSpec();

    protected ApplicationSpec.Dictionary readRawScriptResource(
        String resourceName,
        HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec) {
      return hostSpec.toApplicationDictionary(
          hostSpec.readObjectNode(resourceName));
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

  static TestSuiteDescriptorLoader createInstance(Class<? extends TestSuiteDescriptorLoader> klass, Config config) {
    try {
      return klass.getConstructor(Config.class).newInstance(config);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw ScriptiveUnitException.wrapIfNecessary(e);
    }
  }
}
