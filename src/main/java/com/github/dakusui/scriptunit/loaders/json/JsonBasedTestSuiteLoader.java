package com.github.dakusui.scriptunit.loaders.json;

import com.github.dakusui.scriptunit.loaders.TestSuiteLoader;
import com.github.dakusui.scriptunit.loaders.json.JsonBeans.TestSuiteDescriptorBean;
import com.github.dakusui.scriptunit.model.TestSuiteDescriptor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import java.io.IOException;
import java.util.AbstractList;

import static com.github.dakusui.scriptunit.core.Utils.*;
import static com.github.dakusui.scriptunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptunit.exceptions.SyntaxException.*;

public class JsonBasedTestSuiteLoader extends TestSuiteLoader.Base {

  private static final String EXTENDS_KEYWORD = "$extends";
  /**
   * A resource that holds default values of ScriptiveUnit.
   */
  private static final String DEFAULTS_JSON   = "defaults.json";

  @SuppressWarnings("WeakerAccess")
  protected JsonBasedTestSuiteLoader(Class<?> driverClass, String resourceName) {
    super(resourceName, driverClass);
  }

  @Override
  protected TestSuiteDescriptor loadTestSuite(Class<?> driverClass, String scriptResourceName) {
    try {
      return new ObjectMapper()
          .readValue(readScript(scriptResourceName), TestSuiteDescriptorBean.class)
          .create(driverClass.newInstance());
    } catch (IOException | IllegalAccessException | InstantiationException e) {
      throw wrap(e);
    }
  }

  protected ObjectNode readObjectNodeWithMerging(String resourceName) {
    ObjectNode child = checkObjectNode(readJsonNodeFromStream(openResourceAsStream(resourceName)));
    ObjectNode work = JsonNodeFactory.instance.objectNode();
    if (child.has(EXTENDS_KEYWORD)) {
      getParentsOf(child).forEach(s -> deepMerge(checkObjectNode(readObjectNodeWithMerging(s)), work));
    }
    return deepMerge(child, work);
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

  public static class Factory implements TestSuiteLoader.Factory {
    @Override
    public TestSuiteLoader create(String resourceName, Class<?> driverClass) {
      return new JsonBasedTestSuiteLoader(driverClass, resourceName);
    }
  }
}
