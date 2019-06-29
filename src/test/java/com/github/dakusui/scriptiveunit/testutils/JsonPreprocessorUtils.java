package com.github.dakusui.scriptiveunit.testutils;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import java.util.AbstractList;

import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.nonArray;
import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.nonText;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;

public enum JsonPreprocessorUtils {
  ;

  private static TextNode requireTextNode(JsonNode curr) {
    return (TextNode) check(curr, v -> curr.isTextual(), () -> nonText(curr));
  }

  private static ArrayNode requireArrayNode(JsonNode curr) {
    return (ArrayNode) check(curr, v -> curr.isArray(), () -> nonArray(curr));
  }

  public static AbstractList<String> getParentsOf(final ObjectNode child, final String parentAttributeName) {
    return new AbstractList<String>() {
      ArrayNode parents = child.has(parentAttributeName) ?
          requireArrayNode(child.get(parentAttributeName)) :
          JsonNodeFactory.instance.arrayNode();

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
