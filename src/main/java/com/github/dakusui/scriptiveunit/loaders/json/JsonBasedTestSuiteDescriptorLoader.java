package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.loaders.beans.TestSuiteDescriptorBean;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.session.Session;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.List;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.loaders.json.JsonPreprocessorUtils.checkObjectNode;
import static com.github.dakusui.scriptiveunit.loaders.json.JsonPreprocessorUtils.getParentsOf;

public class JsonBasedTestSuiteDescriptorLoader extends TestSuiteDescriptorLoader.Base {

  protected static final String EXTENDS_KEYWORD = "$extends";
  /**
   * A resource that holds default values of ScriptiveUnit.
   */
  protected static final String DEFAULTS_JSON   = "defaults/values.json";

  @SuppressWarnings("unused")
  public JsonBasedTestSuiteDescriptorLoader(Config config) {
    super(config);
  }

  @Override
  public TestSuiteDescriptor loadTestSuiteDescriptor(Session session) {
    try {
      return getJsonTestSuiteDescriptorBean(session).create(session);
    } catch (IOException e) {
      throw wrap(e);
    }
  }

  protected ObjectNode readObjectNodeWithMerging(String resourceName) {
    ObjectNode work = createObjectNode();
    ObjectNode child = preprocess(readResource(resourceName), getPreprocessors());
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
    ObjectNode ret = readDefaultValues();
    return removeInheritanceDirective(deepMerge(work, ret));
  }

  // TEMPLATE
  private ObjectNode readScriptHandlingInheritance(Session session) {
    return readScriptHandlingInheritance(session.getConfig().getScriptResourceName());
  }

  // CUSTOMIZATION POINT
  private ObjectNode createObjectNode() {
    return JsonNodeFactory.instance.objectNode();
  }

  // CUSTOMIZATION POINT
  private boolean hasInheritanceDirective(ObjectNode child) {
    return child.has(EXTENDS_KEYWORD);
  }

  // CUSTOMIZATION POINT
  private List<String> getParents(ObjectNode child) {
    return getParentsOf(child, EXTENDS_KEYWORD);
  }

  // CUSTOMIZATION POINT
  private ObjectNode deepMerge(ObjectNode work, ObjectNode base) {
    return JsonUtils.deepMerge(work, base);
  }

  // CUSTOMIZATION POINT
  protected ObjectNode preprocess(JsonNode inputNode, List<JsonPreprocessor> preprocessors) {
    for (JsonPreprocessor each : preprocessors) {
      inputNode = JsonPreprocessorUtils.translate(each, inputNode);
    }
    return checkObjectNode(inputNode);
  }

  // CUSTOMIZATION POINT
  private TestSuiteDescriptorBean getJsonTestSuiteDescriptorBean(Session session) throws IOException {
    return new ObjectMapper().readValue(
        readScriptHandlingInheritance(session),
        JsonTestSuiteDescriptorBean.class);
  }

  // CUSTOMIZATION POINT
  private ObjectNode removeInheritanceDirective(ObjectNode ret) {
    ret.remove(EXTENDS_KEYWORD);
    return ret;
  }

  // CUSTOMIZATION POINT
  private ObjectNode readDefaultValues() {
    return readResource(DEFAULTS_JSON);
  }

  // CUSTOMIZATION POINT
  private ObjectNode readResource(String resourceName) {
    return checkObjectNode(JsonUtils.readJsonNodeFromStream(ReflectionUtils.openResourceAsStream(resourceName)));
  }
}
