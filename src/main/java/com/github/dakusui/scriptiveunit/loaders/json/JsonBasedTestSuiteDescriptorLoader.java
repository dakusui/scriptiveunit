package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.Preprocessor;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.session.Session;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.List;

import static com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.dict;

public class JsonBasedTestSuiteDescriptorLoader extends TestSuiteDescriptorLoader.Base {

  protected final HostSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> hostSpec = hostLanguage();

  private final ApplicationSpec applicationSpec = modelSpec();

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

  // TEMPLATE
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

  // TEMPLATE
  protected List<Preprocessor> getPreprocessors() {
    return applicationSpec.preprocessors();
  }

  // TEMPLATE
  protected ApplicationSpec.Dictionary readScriptHandlingInheritance(String scriptResourceName) {
    ApplicationSpec.Dictionary work = readObjectNodeWithMerging(scriptResourceName);
    ApplicationSpec.Dictionary ret = applicationSpec.createDefaultValues();
    return applicationSpec.removeInheritanceDirective(ApplicationSpec.deepMerge(work, ret));
  }

  // TEMPLATE
  protected ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary inputNode, List<Preprocessor> preprocessors) {
    for (Preprocessor each : preprocessors) {
      inputNode = ApplicationSpec.preprocess(inputNode, each);
    }
    return inputNode;
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
