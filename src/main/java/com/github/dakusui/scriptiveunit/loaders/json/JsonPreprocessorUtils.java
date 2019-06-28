package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.loaders.Preprocessor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import java.util.AbstractList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.nonArray;
import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.nonObject;
import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.nonText;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;
import static java.util.Objects.requireNonNull;

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

  public static ObjectNode translate(Preprocessor<JsonNode> jsonPreprocessor, ObjectNode rootNode) {
    return (ObjectNode) translate(jsonPreprocessor, Preprocessor.Path.createRoot(), rootNode);
  }

  public static JsonNode translate(Preprocessor<JsonNode> jsonPreprocessor, Preprocessor.Path pathToTarget, JsonNode targetElement) {
    if (jsonPreprocessor.matches(pathToTarget)) {
      return jsonPreprocessor.translate(targetElement);
    }
    JsonNode work;
    if (targetElement instanceof ObjectNode) {
      work = targetElement;
      ((Iterable<String>) () -> requireNonNull(targetElement.getFieldNames())).forEach(
          (String attributeName) ->
              ((ObjectNode) targetElement).put(
                  attributeName,
                  translate(jsonPreprocessor, pathToTarget.createChild(attributeName), targetElement.get(attributeName))
              ));
    } else if (targetElement instanceof ArrayNode) {
      AtomicInteger i = new AtomicInteger(0);
      work = new ArrayNode(JsonNodeFactory.instance);
      targetElement.forEach(
          (JsonNode jsonNode) -> ((ArrayNode) work).add(
              translate(
                  jsonPreprocessor,
                  pathToTarget.createChild(i.getAndIncrement()),
                  jsonNode
              )));
    } else {
      work = targetElement;
    }
    return Objects.equals(targetElement, work) ?
        targetElement :
        work;
  }
}
