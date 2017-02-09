package com.github.dakusui.scriptunit.loaders.json;

import com.github.dakusui.scriptunit.exceptions.SyntaxException;
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
import static com.github.dakusui.scriptunit.exceptions.ScriptUnitException.wrap;

public class JsonBasedTestSuiteLoader extends TestSuiteLoader.Base {

  public static final String EXTENDS_KEYWORD = "$extends";

  @SuppressWarnings("WeakerAccess")
  protected JsonBasedTestSuiteLoader(String resourceName, Class<?> driverClass) {
    super(resourceName, driverClass);
  }

  @Override
  protected TestSuiteDescriptor loadTestSuite(String resourceName, Class<?> driverClass) {
    try {
      return new ObjectMapper()
          .readValue(readObjectNodeWithMerging(resourceName), TestSuiteDescriptorBean.class)
          .create(driverClass);
    } catch (IOException e) {
      throw wrap(e);
    }
  }

  private ObjectNode readObjectNodeWithMerging(String resourceName) {
    ObjectNode child = checkObjectNode(readJsonNodeFromStream(openResourceAsStream(resourceName)));
    ObjectNode work = JsonNodeFactory.instance.objectNode();
    if (child.has(EXTENDS_KEYWORD)) {
      getParentsOf(child).forEach(s -> deepMerge(checkObjectNode(readObjectNodeWithMerging(s)), work));
    }
    ObjectNode ret = deepMerge(child, work);
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
    JsonNode checked = check(curr, v -> curr.isTextual(), () -> SyntaxException.nonText(curr));
    return (TextNode) checked;
  }

  private ObjectNode checkObjectNode(JsonNode curr) {
    JsonNode checked = check(curr, v -> curr.isObject(), () -> SyntaxException.nonObject(curr));
    return (ObjectNode) checked;
  }

  private ArrayNode checkArrayNode(JsonNode curr) {
    JsonNode checked = check(curr, v -> curr.isArray(), () -> SyntaxException.nonArray(curr));
    return (ArrayNode) checked;
  }

  public static class Factory implements TestSuiteLoader.Factory {
    @Override
    public TestSuiteLoader create(String resourceName, Class<?> driverClass) {
      return new JsonBasedTestSuiteLoader(resourceName, driverClass);
    }
  }
}
