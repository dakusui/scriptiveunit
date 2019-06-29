package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.lang.Preprocessor;
import com.github.dakusui.scriptiveunit.model.session.Session;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.dict;
import static java.util.Objects.requireNonNull;

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

    private final Config config;

    protected final HostSpec<NODE, OBJECT, ARRAY, NODE> hostSpec = hostLanguage();

    final ApplicationSpec applicationSpec = modelSpec();

    public Base(Config config) {
      this.config = requireNonNull(config);
    }

    public Config getConfig() {
      return this.config;
    }

    protected ApplicationSpec.Dictionary readObjectNodeWithMerging(String resourceName) {
      ApplicationSpec.Dictionary child = preprocess(
          hostSpec.toApplicationDictionary(
              hostSpec.readObjectNode(resourceName)),
          getPreprocessors());

      ApplicationSpec.Dictionary work_ = dict();
      for (String s : modelSpec().parentsOf(child))
        work_ = ApplicationSpec.deepMerge(readObjectNodeWithMerging(s), work_);
      return ApplicationSpec.deepMerge(child, work_);
    }

    protected List<Preprocessor> getPreprocessors() {
      return applicationSpec.preprocessors();
    }

    protected ApplicationSpec.Dictionary readScriptHandlingInheritance(String scriptResourceName) {
      ApplicationSpec.Dictionary work = readObjectNodeWithMerging(scriptResourceName);
      ApplicationSpec.Dictionary ret = applicationSpec.createDefaultValues();
      return applicationSpec.removeInheritanceDirective(ApplicationSpec.deepMerge(work, ret));
    }

    protected ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary inputNode, List<Preprocessor> preprocessors) {
      for (Preprocessor each : preprocessors) {
        inputNode = ApplicationSpec.preprocess(inputNode, each);
      }
      return inputNode;
    }

    abstract protected ApplicationSpec modelSpec();

    abstract protected HostSpec<NODE, OBJECT, ARRAY, NODE> hostLanguage();

  }

  static TestSuiteDescriptorLoader createInstance(Class<? extends TestSuiteDescriptorLoader> klass, Config config) {
    try {
      return klass.getConstructor(Config.class).newInstance(config);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw wrap(e);
    }
  }
}
