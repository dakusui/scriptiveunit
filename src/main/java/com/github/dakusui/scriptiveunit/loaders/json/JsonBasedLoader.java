package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Preprocessor;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBeans.TestSuiteDescriptorBean;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.List;

import static com.github.dakusui.scriptiveunit.core.Utils.deepMerge;
import static com.github.dakusui.scriptiveunit.core.Utils.openResourceAsStream;
import static com.github.dakusui.scriptiveunit.core.Utils.readJsonNodeFromStream;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;

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
      return new ObjectMapper()
          .readValue(
              readScript(session.getConfig().getScriptResourceName()),
              TestSuiteDescriptorBean.class
          ).create(session);
    } catch (IOException e) {
      throw wrap(e);
    }
  }

  protected ObjectNode readObjectNodeWithMerging(String resourceName) {
    ObjectNode child = JsonPreprocessorUtils.checkObjectNode(preprocess(readJsonNodeFromStream(openResourceAsStream(resourceName))));
    ObjectNode work = JsonNodeFactory.instance.objectNode();
    if (child.has(EXTENDS_KEYWORD)) {
      JsonPreprocessorUtils.getParentsOf(child, EXTENDS_KEYWORD).forEach(s -> deepMerge(JsonPreprocessorUtils.checkObjectNode(readObjectNodeWithMerging(s)), work));
    }
    return deepMerge(child, work);
  }

  protected List<Preprocessor> getPreprocessors() {
    return JsonPreprocessorUtils.preprocessors();
  }

  protected ObjectNode readScript(String scriptResourceName) {
    ObjectNode work = readObjectNodeWithMerging(scriptResourceName);
    ObjectNode ret = JsonPreprocessorUtils.checkObjectNode(readJsonNodeFromStream(openResourceAsStream(DEFAULTS_JSON)));
    ret = deepMerge(work, ret);
    ret.remove(EXTENDS_KEYWORD);
    return ret;
  }

  protected JsonNode preprocess(JsonNode inputNode) {
    return JsonPreprocessorUtils.preprocess(inputNode, getPreprocessors());
  }
}
