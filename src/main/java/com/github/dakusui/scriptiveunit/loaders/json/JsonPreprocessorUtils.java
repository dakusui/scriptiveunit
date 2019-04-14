package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.core.Preprocessor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import java.util.AbstractList;
import java.util.List;

import static com.github.dakusui.scriptiveunit.core.JsonUtils.*;
import static com.github.dakusui.scriptiveunit.core.Utils.check;
import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.*;
import static java.util.Collections.singletonList;

public enum JsonPreprocessorUtils {
  ;

  public static ObjectNode checkObjectNode(JsonNode curr) {
    return (ObjectNode) check(curr, v -> curr.isObject(), () -> nonObject(curr));
  }

  private static TextNode checkTextNode(JsonNode curr) {
    return (TextNode) check(curr, v -> curr.isTextual(), () -> nonText(curr));
  }

  private static ArrayNode checkArrayNode(JsonNode curr) {
    return (ArrayNode) check(curr, v -> curr.isArray(), () -> nonArray(curr));
  }

  static List<Preprocessor> preprocessors() {
    return singletonList(preprocessor(
        JsonPreprocessorUtils::toUniformedObjectNode,
        pathMatcher("factorSpace", "factors", ".*")));
  }

  private static JsonNode toUniformedObjectNode(JsonNode targetElement) {
    return targetElement instanceof ObjectNode ?
        targetElement :
        object()
            .$("type", "simple")
            .$("args", toArrayNode(targetElement))
            .build();
  }

  private static Object toArrayNode(JsonNode targetElement) {
    return targetElement instanceof ArrayNode ?
        targetElement :
        array().$(targetElement).build();
  }

  public static AbstractList<String> getParentsOf(final ObjectNode child, final String parentAttributeName) {
    return new AbstractList<String>() {
      ArrayNode parents = checkArrayNode(child.get(parentAttributeName));

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

  static JsonNode preprocess(JsonNode ret, List<Preprocessor> preprocessors) {
    for (Preprocessor each : preprocessors) {
      ret = Preprocessor.translate(each, ret);
    }
    return ret;
  }
}
