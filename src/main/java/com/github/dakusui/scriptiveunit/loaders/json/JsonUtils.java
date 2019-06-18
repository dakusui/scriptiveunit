package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.utils.Checks;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.NumericNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import java.io.IOException;
import java.io.InputStream;

import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.mergeFailed;
import static java.util.Objects.requireNonNull;

public enum JsonUtils {
  ;

  /**
   * Merges two object nodes and merged object node will be returned.
   * If {@code a} and {@code b} has the same attributes the value comes from
   * {@code a} will override the other's.
   * Values in both {@code a} and {@code b} will not be changed and new object node
   * will be returned
   *
   * @param a An object node.
   * @param b An object node.
   * @return A merged object node that is newly created.
   */
  public static Object mergeObjectNodes(ObjectNode a, ObjectNode b) {
    return deepMerge(a, (ObjectNode) JsonNodeFactory.instance.objectNode().putAll(b));
  }

  /**
   * Merges {@code source} object node into {@code target} and returns {@code target}.
   *
   * @param source An object node to be merged into {@code target}.
   * @param target An object node to be put attributes in {@code source}
   * @return {@code target} object node
   */
  public static ObjectNode deepMerge(ObjectNode source, ObjectNode target) {
    requireNonNull(source);
    requireNonNull(target);
    for (String key : (Iterable<String>) source::getFieldNames) {
      JsonNode sourceValue = source.get(key);
      if (!target.has(key)) {
        // new value for "key":
        target.put(key, sourceValue);
      } else {
        // existing value for "key" - recursively deep merge:
        if (sourceValue.isObject()) {
          ObjectNode sourceObject = (ObjectNode) sourceValue;
          JsonNode targetValue = target.get(key);
          Checks.check(targetValue.isObject(), () -> mergeFailed(source, target, key));
          deepMerge(sourceObject, (ObjectNode) targetValue);
        } else {
          target.put(key, sourceValue);
        }
      }
    }
    return target;
  }

  public static JsonNode readJsonNodeFromStream(InputStream is) {
    try {
      return new ObjectMapper().readTree(is);
    } catch (IOException e) {
      throw ScriptiveUnitException.wrap(e, "Non-welformed input is given.");
    }
  }

  public interface Builder<T extends JsonNode> {
    T build();
  }

  public static class ObjectNodeBuilder implements Builder<ObjectNode> {
    private ObjectNode value = JsonNodeFactory.instance.objectNode();

    public ObjectNodeBuilder $(String fieldName, Object value) {
      this.value.put(fieldName, toJsonNode(value));
      return this;
    }

    @Override
    public ObjectNode build() {
      return value;
    }
  }

  public static class ArrayNodeBuilder implements Builder<ArrayNode> {
    private ArrayNode value = JsonNodeFactory.instance.arrayNode();

    public ArrayNodeBuilder $(Object value) {
      this.value.add(toJsonNode(value));
      return this;
    }

    @Override
    public ArrayNode build() {
      return value;
    }
  }

  public static ObjectNodeBuilder object() {
    return new ObjectNodeBuilder();
  }

  public static ArrayNodeBuilder array() {
    return new ArrayNodeBuilder();
  }

  public static NumericNode numeric(Number v) {
    return JsonNodeFactory.instance.numberNode(Utils.toBigDecimal(v));
  }

  public static TextNode text(String text) {
    return JsonNodeFactory.instance.textNode(text);
  }

  public static JsonNode toJsonNode(Object value) {
    if (value instanceof JsonNode)
      return (JsonNode) value;
    if (value instanceof String)
      return text((String) value);
    if (value instanceof Number)
      return numeric((Number) value);
    throw new UnsupportedOperationException();
  }
}
