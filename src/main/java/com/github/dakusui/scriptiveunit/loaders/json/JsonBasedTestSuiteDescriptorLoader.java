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

import static com.github.dakusui.scriptiveunit.loaders.json.JsonPreprocessorUtils.checkObjectNode;

public class JsonBasedTestSuiteDescriptorLoader extends TestSuiteDescriptorLoader.Base {

  /**
   * A resource that holds default values of ScriptiveUnit.
   */
  protected static final String DEFAULTS_JSON = "defaults/values.json";

  private static final HostLanguage<JsonNode, ObjectNode, ArrayNode, JsonNode> hostLanguage = new HostLanguage.Json();
  private static final ModelSpec                                               modelSpec    = new ModelSpec.Standard();

  @SuppressWarnings("unused")
  public JsonBasedTestSuiteDescriptorLoader(Config config) {
    super(config);
  }

  // TEMPLATE
  @Override
  public TestSuiteDescriptor loadTestSuiteDescriptor(Session session) {
    return mapObjectNode(readScriptHandlingInheritance(session), JsonTestSuiteDescriptorBean.class).create(session);
  }

  // TEMPLATE
  protected ObjectNode readObjectNodeWithMerging(String resourceName) {
    ObjectNode work = hostLanguage.newObjectNode();
    ObjectNode child = preprocess(readObjectNode(resourceName), getPreprocessors());
    if (hasInheritanceDirective(child))
      getParents(child).forEach(s -> deepMerge(readObjectNodeWithMerging(s), work));
    return deepMerge(child, work);
  }

  // TEMPLATE
  protected List<JsonPreprocessor> getPreprocessors() {
    // TODO
    return JsonPreprocessorUtils.preprocessors();
  }

  // TEMPLATE
  protected ObjectNode readScriptHandlingInheritance(String scriptResourceName) {
    ObjectNode work = readObjectNodeWithMerging(scriptResourceName);
    ObjectNode ret = hostLanguage.translate(modelSpec.createDefaultValues());
    return hostLanguage.removeInheritanceDirective(deepMerge(work, ret));
  }

  // TEMPLATE
  private ObjectNode readScriptHandlingInheritance(Session session) {
    return readScriptHandlingInheritance(session.getConfig().getScriptResourceName());
  }

  // TEMPLATE
  protected ObjectNode preprocess(ObjectNode inputNode, List<? extends Preprocessor> preprocessors) {
    for (Preprocessor each : preprocessors) {
      inputNode = performPreprocess(inputNode, (JsonPreprocessor) each);
    }
    return checkObjectNode(inputNode);
  }

  // CUSTOMIZATION POINT
  private ObjectNode performPreprocess(ObjectNode inputNode, JsonPreprocessor jsonPreprocessor) {
    return (ObjectNode) JsonPreprocessorUtils.translate(jsonPreprocessor, inputNode);
  }

  // CUSTOMIZATION POINT
  private ObjectNode deepMerge(ObjectNode work, ObjectNode base) {
    return hostLanguage.deepMerge(work, base);
  }

  // CUSTOMIZATION POINT
  private <V> V mapObjectNode(ObjectNode rootNode, Class<V> valueType) {
    return hostLanguage.mapObjectNode(rootNode, valueType);
  }

  // CUSTOMIZATION POINT
  private boolean hasInheritanceDirective(ObjectNode child) {
    return hostLanguage.hasInheritanceDirective(child);
  }

  // CUSTOMIZATION POINT
  private List<String> getParents(ObjectNode child) {
    return hostLanguage.getParents(child);
  }

  // CUSTOMIZATION POINT
  private ObjectNode readObjectNode(String resourceName) {
    return hostLanguage.readObjectNode(resourceName);
  }
}
