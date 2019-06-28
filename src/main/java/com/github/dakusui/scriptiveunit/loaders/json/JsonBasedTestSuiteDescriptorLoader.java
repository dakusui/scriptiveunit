package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.Preprocessor;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.session.Session;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.List;

import static com.github.dakusui.scriptiveunit.loaders.json.ModelSpec.dict;

public class JsonBasedTestSuiteDescriptorLoader extends TestSuiteDescriptorLoader.Base {

  protected final HostLanguage<JsonNode, ObjectNode, ArrayNode, JsonNode> hostLanguage = hostLanguage();

  private final ModelSpec modelSpec = modelSpec();

  @SuppressWarnings("unused")
  public JsonBasedTestSuiteDescriptorLoader(Config config) {
    super(config);
  }

  // TEMPLATE
  @Override
  public TestSuiteDescriptor loadTestSuiteDescriptor(Session session) {
    return hostLanguage.mapObjectNode(
        readScriptHandlingInheritance(session.getConfig().getScriptResourceName()),
        JsonTestSuiteDescriptorBean.class)
        .create(session);
  }

  // TEMPLATE
  protected ModelSpec.Dictionary readObjectNodeWithMerging(String resourceName) {
    ModelSpec.Dictionary child = preprocess(
        hostLanguage.toModelDictionary(
            hostLanguage.readObjectNode(resourceName)),
        getPreprocessors());

    ModelSpec.Dictionary work_ = dict();
    for (String s : hostLanguage.getParents(hostLanguage.translate(child)))
      work_ = ModelSpec.deepMerge(readObjectNodeWithMerging(s), work_);
    return ModelSpec.deepMerge(child, work_);
  }

  // TEMPLATE
  protected List<Preprocessor> getPreprocessors() {
    return modelSpec.preprocessors();
  }

  // TEMPLATE
  protected ObjectNode readScriptHandlingInheritance(String scriptResourceName) {
    ModelSpec.Dictionary work = readObjectNodeWithMerging(scriptResourceName);
    ModelSpec.Dictionary ret = modelSpec.createDefaultValues();
    return hostLanguage.removeInheritanceDirective(hostLanguage.translate(ModelSpec.deepMerge(work, ret)));
  }

  // TEMPLATE
  protected ModelSpec.Dictionary preprocess(ModelSpec.Dictionary inputNode, List<Preprocessor> preprocessors) {
    for (Preprocessor each : preprocessors) {
      inputNode = ModelSpec.preprocess(inputNode, each);
    }
    return inputNode;
  }

  protected ModelSpec modelSpec() {
    return new ModelSpec.Standard();
  }

  protected HostLanguage<JsonNode, ObjectNode, ArrayNode, JsonNode> hostLanguage() {
    return new HostLanguage.Json();
  }
}
