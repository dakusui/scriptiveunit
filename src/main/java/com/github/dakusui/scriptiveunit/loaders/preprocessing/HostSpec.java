package com.github.dakusui.scriptiveunit.loaders.preprocessing;

import com.github.dakusui.scriptiveunit.utils.JsonUtils;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec.isArray;
import static com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec.isAtom;
import static com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec.isDictionary;
import static com.github.dakusui.scriptiveunit.utils.CoreUtils.toBigDecimal;
import static com.github.dakusui.scriptiveunit.utils.JsonUtils.requireObjectNode;
import static java.lang.String.format;

public interface HostSpec<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
  OBJECT newObjectNode();

  ARRAY newArrayNode();

  ATOM newAtomNode(Object value);

  boolean isObjectNode(NODE node);

  boolean isArrayNode(NODE node);

  boolean isAtomNode(NODE node);

  Stream<String> keysOf(OBJECT objectNode);

  Stream<NODE> elementsOf(ARRAY arrayNode);

  NODE valueOf(OBJECT object, String key);

  Object valueOf(ATOM atom);

  OBJECT readObjectNode(String resourceName);

  void putToObject(OBJECT ret, String eachKey, NODE jsonNodeValue);

  void addToArray(ARRAY ret, NODE eachNode);

  OBJECT toHostObject(ApplicationSpec.Dictionary dictionary);

  ApplicationSpec.Dictionary toApplicationDictionary(OBJECT object);

  ApplicationSpec.Dictionary readRawScript(String resourceName);

  interface Default<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> extends HostSpec<NODE, OBJECT, ARRAY, ATOM> {
    @Override
    default OBJECT toHostObject(ApplicationSpec.Dictionary dictionary) {
      OBJECT ret = newObjectNode();
      dictionary.streamKeys()
          .forEach((String eachKey) -> {
            ApplicationSpec.Node nodeValue = dictionary.valueOf(eachKey);
            NODE jsonNodeValue = toHostNode(nodeValue);
            putToObject(ret, eachKey, jsonNodeValue);
          });
      return ret;
    }

    @Override
    default ApplicationSpec.Dictionary toApplicationDictionary(OBJECT object) {
      return ApplicationSpec.dict(
          keysOf(object)
              .map(k -> ApplicationSpec.$(k, toApplicationNode(valueOf(object, k)))).toArray(ApplicationSpec.Dictionary.Entry[]::new)
      );
    }

    default ARRAY toHostArray(ApplicationSpec.Array array) {
      ARRAY ret = newArrayNode();
      array.stream().forEach((ApplicationSpec.Node eachNode) -> addToArray(ret, toHostNode(eachNode)));
      return ret;
    }

    default ATOM toHostAtom(ApplicationSpec.Atom atom) {
      return newAtomNode(atom.get());
    }

    default NODE toHostNode(ApplicationSpec.Node modelNode) {
      NODE nodeValue;
      if (isAtom(modelNode))
        nodeValue = toHostAtom((ApplicationSpec.Atom) modelNode);
      else if (isArray(modelNode))
        nodeValue = toHostArray((ApplicationSpec.Array) modelNode);
      else if (isDictionary(modelNode))
        nodeValue = toHostObject((ApplicationSpec.Dictionary) modelNode);
      else
        throw new RuntimeException(format("Unsupported value was given: '%s'", modelNode));
      return nodeValue;
    }

    default ApplicationSpec.Array toApplicationArray(ARRAY array) {
      return ApplicationSpec.array(elementsOf(array)
          .map(this::toApplicationNode)
          .toArray(ApplicationSpec.Node[]::new));
    }

    default ApplicationSpec.Atom toModelAtom(ATOM atom) {
      return ApplicationSpec.atom(valueOf(atom));
    }

    @SuppressWarnings("unchecked")
    default ApplicationSpec.Node toApplicationNode(NODE node) {
      if (isAtomNode(node))
        return toModelAtom((ATOM) node);
      if (isArrayNode(node))
        return toApplicationArray((ARRAY) node);
      if (isObjectNode(node))
        return toApplicationDictionary((OBJECT) node);
      throw new UnsupportedOperationException();
    }
  }

  class Json implements HostSpec.Default<JsonNode, ObjectNode, ArrayNode, JsonNode> {
    static final String EXTENDS_KEYWORD = "$extends";

    @Override
    public ObjectNode newObjectNode() {
      return JsonNodeFactory.instance.objectNode();
    }

    @Override
    public ArrayNode newArrayNode() {
      return JsonNodeFactory.instance.arrayNode();
    }

    @Override
    public JsonNode newAtomNode(Object value) {
      if (value == null)
        return JsonNodeFactory.instance.nullNode();
      if (value instanceof Number) {
        if (value instanceof Integer)
          return JsonNodeFactory.instance.numberNode((Integer) value);
        if (value instanceof Long)
          return JsonNodeFactory.instance.numberNode((Long) value);
        return JsonNodeFactory.instance.numberNode(toBigDecimal((Number) value));
      }
      if (value instanceof String)
        return JsonNodeFactory.instance.textNode((String) value);
      throw new RuntimeException(format("Unsupported value was given: '%s'", value));
    }

    @Override
    public boolean isObjectNode(JsonNode jsonNode) {
      return jsonNode.isObject();
    }

    @Override
    public boolean isArrayNode(JsonNode jsonNode) {
      return jsonNode.isArray();
    }

    @Override
    public boolean isAtomNode(JsonNode jsonNode) {
      return !(jsonNode.isObject() || jsonNode.isArray());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Stream<String> keysOf(ObjectNode objectNode) {
      return StreamSupport.stream(((Iterable<String>) objectNode::getFieldNames).spliterator(), false);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Stream<JsonNode> elementsOf(ArrayNode arrayNode) {
      return StreamSupport.stream(((Iterable<JsonNode>) arrayNode::getElements).spliterator(), false);
    }

    @Override
    public JsonNode valueOf(ObjectNode object, String key) {
      return object.get(key);
    }

    @Override
    public Object valueOf(JsonNode jsonNode) {
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
      if (jsonNode.isBigInteger())
        return jsonNode.getBigIntegerValue();
      if (jsonNode.isBigDecimal())
        return jsonNode.getDecimalValue();
      if (jsonNode.isNumber()) {
        return jsonNode.getDecimalValue();
      }
      throw new UnsupportedOperationException();
    }

    @Override
    public ObjectNode readObjectNode(String resourceName) {
      return requireObjectNode(JsonUtils.readJsonNodeFromStream(ReflectionUtils.openResourceAsStream(resourceName)));
    }

    @Override
    public void putToObject(ObjectNode ret, String eachKey, JsonNode jsonNodeValue) {
      ret.put(eachKey, jsonNodeValue);
    }

    @Override
    public void addToArray(ArrayNode ret, JsonNode eachNode) {
      ret.add(eachNode);
    }

    @Override
    public ApplicationSpec.Dictionary readRawScript(String resourceName) {
      return this.toApplicationDictionary(this.readObjectNode(resourceName));
    }
  }
}
