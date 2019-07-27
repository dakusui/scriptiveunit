package com.github.dakusui.scriptiveunit.utils;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.dakusui.scriptiveunit.exceptions.Exceptions.nonObject;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapMinimally;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitUnclassifiedException.unclassifiedException;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;
import static com.github.dakusui.scriptiveunit.utils.CoreUtils.toBigDecimal;

public enum JsonUtils {
  ;

  public static JsonNode readJsonNodeFromStream(InputStream is) {
    try {
      return new ObjectMapper().readTree(is);
    } catch (IOException e) {
      throw wrapMinimally("Malformed input is given.", e);
    }
  }

  public static ObjectNode requireObjectNode(JsonNode curr) {
    return (ObjectNode) check(curr, v -> curr.isObject(), () -> nonObject(curr));
  }

  public static JsonNode toJsonNode(Object value) {
    if (value == null)
      return JsonNodeFactory.instance.nullNode();
    if (value instanceof Number) {
      if (value instanceof Integer)
        return JsonNodeFactory.instance.numberNode((Integer) value);
      if (value instanceof Long)
        return JsonNodeFactory.instance.numberNode((Long) value);
      if (value instanceof Float)
        return JsonNodeFactory.instance.numberNode((Float) value);
      if (value instanceof Double)
        return JsonNodeFactory.instance.numberNode((Double) value);
      return JsonNodeFactory.instance.numberNode(toBigDecimal((Number) value));
    }
    if (value instanceof Boolean)
      return JsonNodeFactory.instance.booleanNode((Boolean) value);
    if (value instanceof String)
      return JsonNodeFactory.instance.textNode((String) value);
    throw unclassifiedException("Unsupported value:<%s> was given", value);
  }

  public static Object toPlainObject(JsonNode jsonNode) {
    if (jsonNode.isNull())
      return null;
    if (jsonNode.isTextual())
      return jsonNode.asText();
    if (jsonNode.isBoolean())
      return jsonNode.asBoolean();
    if (jsonNode.isInt())
      return jsonNode.asInt();
    if (jsonNode.isLong())
      return jsonNode.asLong();
    if (jsonNode.isDouble())
      return jsonNode.asDouble();
    if (jsonNode.isBigInteger())
      return jsonNode.getBigIntegerValue();
    if (jsonNode.isBigDecimal())
      return jsonNode.getDecimalValue();
    if (jsonNode.isNumber()) {
      return jsonNode.getDecimalValue();
    }
    throw unclassifiedException("Unsupported JSON node:<%s> was given", jsonNode);
  }

  @FunctionalInterface
  public
  interface NodeFactory<N extends JsonNode> extends Helper, Supplier<N> {
    static ObjectNode emptyObject() {
      return JsonNodeFactory.instance.objectNode();
    }

    @SuppressWarnings("unchecked")
    default N get() {
      return (N) create();
    }

    JsonNode create();
  }

  public interface Helper {
    default ObjectNode obj(Entry... entries) {
      ObjectNode ret = JsonNodeFactory.instance.objectNode();
      for (Consumer<ObjectNode> each : entries)
        each.accept(ret);
      return ret;
    }

    default ArrayNode arr(Object... elements) {
      ArrayNode ret = JsonNodeFactory.instance.arrayNode();
      for (Object each : elements)
        if (each instanceof JsonNode)
          ret.add((JsonNode) each);
        else
          ret.add(toJsonNode(each));
      return ret;
    }

    default Entry $(String key, JsonNode value) {
      return (ObjectNode obj) -> obj.put(key, value);
    }

    default JsonNode $(Object value) {
      return toJsonNode(value);
    }

    interface Entry extends Consumer<ObjectNode> {
    }
  }
}
