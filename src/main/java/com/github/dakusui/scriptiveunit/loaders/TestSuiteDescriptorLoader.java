package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.json.JsonTestSuiteDescriptorBean;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.lang.Preprocessor;
import com.github.dakusui.scriptiveunit.model.session.Session;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.github.dakusui.scriptiveunit.exceptions.ConfigurationException.scriptNotSpecified;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.dict;

public interface TestSuiteDescriptorLoader {
  static TestSuiteDescriptorLoader createTestSuiteDescriptorLoader(
      Class<? extends TestSuiteDescriptorLoader> loaderClass,
      Config config) {
    return createInstance(loaderClass, config);
  }

  Config getConfig();

  TestSuiteDescriptor loadTestSuiteDescriptor(Session session);

  abstract class Base<NODE, OBJECT extends NODE, ARRAY extends NODE> implements
      TestSuiteDescriptorLoader {
    private final HostSpec<NODE, OBJECT, ARRAY, NODE> hostSpec = createHostSpec();

    private final ApplicationSpec applicationSpec = createApplicationSpec();

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
              readScript(session.getConfig().getScriptResourceName()
                  .orElseThrow(() -> scriptNotSpecified(session.getConfig().getScriptResourceNameKey())), applicationSpec, hostSpec)))
          .create(session);
    }

    ApplicationSpec.Dictionary readScript(String scriptResourceName, ApplicationSpec applicationSpec, HostSpec<NODE, OBJECT, ARRAY, NODE> hostSpec) {
      return readScript(
          scriptResourceName,
          applicationSpec.createDefaultValues(),
          applicationSpec, hostSpec);
    }

    protected ApplicationSpec.Dictionary readScript(String scriptResourceName, ApplicationSpec.Dictionary defaultValues, ApplicationSpec applicationSpec, HostSpec<NODE, OBJECT, ARRAY, NODE> hostSpec) {
      return applicationSpec.deepMerge(readScriptHandlingInheritance(scriptResourceName, applicationSpec, hostSpec), defaultValues);
    }

    protected ApplicationSpec.Dictionary readApplicationDictionaryWithMerging(String resourceName, ApplicationSpec applicationSpec, HostSpec<NODE, OBJECT, ARRAY, NODE> hostSpec) {
      ApplicationSpec.Dictionary child = preprocess(
          hostSpec.toApplicationDictionary(
              hostSpec.readObjectNode(resourceName)),
          applicationSpec.preprocessors());

      ApplicationSpec.Dictionary work_ = dict();
      for (String s : applicationSpec.parentsOf(child))
        work_ = applicationSpec.deepMerge(readApplicationDictionaryWithMerging(s, applicationSpec, hostSpec), work_);
      return applicationSpec.deepMerge(child, work_);
    }

    ApplicationSpec.Dictionary readScriptHandlingInheritance(String scriptResourceName, ApplicationSpec applicationSpec, HostSpec<NODE, OBJECT, ARRAY, NODE> hostSpec) {
      return applicationSpec.removeInheritanceDirective(readApplicationDictionaryWithMerging(scriptResourceName, applicationSpec, hostSpec));
    }

    ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary inputNode, List<Preprocessor> preprocessors) {
      for (Preprocessor each : preprocessors) {
        inputNode = ApplicationSpec.preprocess(inputNode, each);
      }
      return inputNode;
    }

    abstract protected ApplicationSpec createApplicationSpec();

    abstract protected HostSpec<NODE, OBJECT, ARRAY, NODE> createHostSpec();


    static private JsonTestSuiteDescriptorBean mapObjectNodeToJsonTestSuiteDescriptorBean(ObjectNode rootNode) {
      try {
        return new ObjectMapper().readValue(
            rootNode,
            JsonTestSuiteDescriptorBean.class);
      } catch (IOException e) {
        throw wrap(e);
      }
    }
  }

  static TestSuiteDescriptorLoader createInstance(Class<? extends TestSuiteDescriptorLoader> klass, Config config) {
    try {
      return klass.getConstructor(Config.class).newInstance(config);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw wrap(e);
    }
  }
}
