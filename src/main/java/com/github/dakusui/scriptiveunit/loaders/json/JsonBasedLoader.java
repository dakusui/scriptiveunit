package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.model.Session;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBeans.TestSuiteDescriptorBean;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.List;

import static com.github.dakusui.scriptiveunit.core.Utils.*;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.loaders.json.JsonPreprocessorUtils.checkObjectNode;
import static com.github.dakusui.scriptiveunit.loaders.json.JsonPreprocessorUtils.getParentsOf;

public class JsonBasedLoader extends TestSuiteDescriptor.Loader.Base {

  protected static final String EXTENDS_KEYWORD = "$extends";
  /**
   * A resource that holds default values of ScriptiveUnit.
   */
  protected static final String DEFAULTS_JSON   = "defaults/values.json";

  @SuppressWarnings("unused")
  public JsonBasedLoader(Config config) {
    super(config);
  }

  @Override
  public TestSuiteDescriptor loadTestSuiteDescriptor(Session session) {
    try {
      return new ObjectMapper().readValue(
          readScript(session.getConfig().getScriptResourceName()),
          TestSuiteDescriptorBean.class)
          .create(session);
    } catch (IOException e) {
      throw wrap(e);
    }
  }

  protected ObjectNode readObjectNodeWithMerging(String resourceName) {
    ObjectNode child = checkObjectNode(preprocess(JsonUtils.readJsonNodeFromStream(openResourceAsStream(resourceName))));
    ObjectNode work = JsonNodeFactory.instance.objectNode();
    if (child.has(EXTENDS_KEYWORD)) {
      getParentsOf(child, EXTENDS_KEYWORD)
          .forEach(s -> JsonUtils.deepMerge(checkObjectNode(readObjectNodeWithMerging(s)), work));
    }
    JsonUtils.deepMerge(child, work);
    return work;
  }

  protected List<Preprocessor> getPreprocessors() {
    return JsonPreprocessorUtils.preprocessors();
  }

  protected ObjectNode readScript(String scriptResourceName) {
    ObjectNode work = readObjectNodeWithMerging(scriptResourceName);
    ObjectNode ret = checkObjectNode(JsonUtils.readJsonNodeFromStream(openResourceAsStream(DEFAULTS_JSON)));
    JsonUtils.deepMerge(work, ret);
    ret.remove(EXTENDS_KEYWORD);
    return ret;
  }

  protected JsonNode preprocess(JsonNode inputNode) {
    return JsonPreprocessorUtils.preprocess(inputNode, getPreprocessors());
  }
}
