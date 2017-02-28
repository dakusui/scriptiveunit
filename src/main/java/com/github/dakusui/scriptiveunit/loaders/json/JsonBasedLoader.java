package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Preprocessor;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBeans.TestSuiteDescriptorBean;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import static com.github.dakusui.scriptiveunit.core.Utils.*;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.*;

public class JsonBasedLoader extends TestSuiteDescriptor.Loader.Base {

  private static final String EXTENDS_KEYWORD = "$extends";
  /**
   * A resource that holds default values of ScriptiveUnit.
   */
  private static final String DEFAULTS_JSON   = "defaults/values.json";

  @ReflectivelyReferenced
  public JsonBasedLoader(Config config) {
    super(config);
  }

  @Override
  public TestSuiteDescriptor loadTestSuiteDescriptor(Session session) {
    try {
      return new ObjectMapper()
          .readValue(
              readScript(session
                  .getConfig()
                  .getScriptResourceName()
              ),
              TestSuiteDescriptorBean.class
          )
          .create(session);
    } catch (IOException e) {
      throw wrap(e);
    }
  }

  protected ObjectNode readObjectNodeWithMerging(String resourceName) {
    ObjectNode child = checkObjectNode(preprocess(readJsonNodeFromStream(openResourceAsStream(resourceName))));
    ObjectNode work = JsonNodeFactory.instance.objectNode();
    if (child.has(EXTENDS_KEYWORD)) {
      getParentsOf(child).forEach(s -> deepMerge(checkObjectNode(readObjectNodeWithMerging(s)), work));
    }
    return deepMerge(child, work);
  }

  protected JsonNode preprocess(JsonNode inputNode) {
    JsonNode ret = inputNode;
    for (Preprocessor each : getPreprocessors()) {
      ret = Preprocessor.translate(each, ret);
    }
    return ret;
  }

  protected List<Preprocessor> getPreprocessors() {
    return Collections.emptyList();
  }

  private ObjectNode readScript(String scriptResourceName) {
    ObjectNode work = readObjectNodeWithMerging(scriptResourceName);
    ObjectNode ret = checkObjectNode(readJsonNodeFromStream(openResourceAsStream(DEFAULTS_JSON)));
    ret = deepMerge(work, ret);
    ret.remove(EXTENDS_KEYWORD);
    return ret;
  }

  private AbstractList<String> getParentsOf(final ObjectNode child) {
    return new AbstractList<String>() {
      ArrayNode parents = checkArrayNode(child.get(EXTENDS_KEYWORD));

      @Override
      public int size() {
        return parents.size();
      }

      @Override
      public String get(int index) {
        return checkTextNode(parents.get(index)).asText();
      }
    };
  }

  private TextNode checkTextNode(JsonNode curr) {
    return (TextNode) check(curr, v -> curr.isTextual(), () -> nonText(curr));
  }

  private ObjectNode checkObjectNode(JsonNode curr) {
    return (ObjectNode) check(curr, v -> curr.isObject(), () -> nonObject(curr));
  }

  private ArrayNode checkArrayNode(JsonNode curr) {
    return (ArrayNode) check(curr, v -> curr.isArray(), () -> nonArray(curr));
  }
}
