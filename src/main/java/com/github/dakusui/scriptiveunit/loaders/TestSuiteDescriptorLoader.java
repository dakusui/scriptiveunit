package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.json.JsonTestSuiteDescriptorBean;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.lang.PreprocessingElement;
import com.github.dakusui.scriptiveunit.model.lang.RawScriptReader;
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

  abstract class Base<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE>
      implements TestSuiteDescriptorLoader {
    private final ApplicationSpec applicationSpec = createApplicationSpec();

    private final HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec = createHostSpec();

    private final RawScriptReader<NODE, OBJECT, ARRAY, ATOM> rawScriptReader = this::readRawScriptResource;

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


    ApplicationSpec.Dictionary readScript(String scriptResourceName, ApplicationSpec applicationSpec, HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec) {
      return readScript(
          scriptResourceName,
          applicationSpec.createDefaultValues(),
          applicationSpec, hostSpec);
    }

    ApplicationSpec.Dictionary readScript(
        String scriptResourceName,
        ApplicationSpec.Dictionary defaultValues,
        ApplicationSpec applicationSpec,
        HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec) {
      return applicationSpec.deepMerge(readScriptHandlingInheritance(scriptResourceName, applicationSpec, hostSpec), defaultValues);
    }

    ApplicationSpec.Dictionary readApplicationDictionaryWithMerging(
        String resourceName,
        ApplicationSpec applicationSpec,
        HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec) {
      ApplicationSpec.Dictionary child = preprocess(
          rawScriptReader.apply(resourceName, hostSpec),
          applicationSpec.preprocessors());

      ApplicationSpec.Dictionary work_ = dict();
      for (String s : applicationSpec.parentsOf(child))
        work_ = applicationSpec.deepMerge(readApplicationDictionaryWithMerging(s, applicationSpec, hostSpec), work_);
      return applicationSpec.deepMerge(child, work_);
    }


    ApplicationSpec.Dictionary readScriptHandlingInheritance(String scriptResourceName, ApplicationSpec applicationSpec, HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec) {
      return applicationSpec.removeInheritanceDirective(readApplicationDictionaryWithMerging(scriptResourceName, applicationSpec, hostSpec));
    }

    ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary inputNode, List<PreprocessingElement> preprocessingElements) {
      for (PreprocessingElement each : preprocessingElements) {
        inputNode = ApplicationSpec.preprocess(inputNode, each);
      }
      return inputNode;
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
