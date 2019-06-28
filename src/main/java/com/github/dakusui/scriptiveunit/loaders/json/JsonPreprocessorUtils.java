package com.github.dakusui.scriptiveunit.loaders.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import java.util.AbstractList;

import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.nonArray;
import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.nonObject;
import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.nonText;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;

public enum JsonPreprocessorUtils {
  ;

  public static ObjectNode requireObjectNode(JsonNode curr) {
    return (ObjectNode) check(curr, v -> curr.isObject(), () -> nonObject(curr));
  }

  private static TextNode requireTextNode(JsonNode curr) {
    return (TextNode) check(curr, v -> curr.isTextual(), () -> nonText(curr));
  }

  private static ArrayNode requireArrayNode(JsonNode curr) {
    return (ArrayNode) check(curr, v -> curr.isArray(), () -> nonArray(curr));
  }

  public static AbstractList<String> getParentsOf(final ObjectNode child, final String parentAttributeName) {
    return new AbstractList<String>() {
      ArrayNode parents = requireArrayNode(child.get(parentAttributeName));

      @Override
      public int size() {
        return parents.size();
      }

      @Override
      public String get(int index) {
        return requireTextNode(parents.get(index)).asText();
      }
    };
  }
}
