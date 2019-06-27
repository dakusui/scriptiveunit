package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.math.BigDecimal;

import static com.github.dakusui.scriptiveunit.loaders.json.JsonBasedTestSuiteDescriptorLoader.EXTENDS_KEYWORD;
import static com.github.dakusui.scriptiveunit.loaders.json.JsonPreprocessorUtils.checkObjectNode;
import static java.lang.String.format;

public interface HostLanguage<OBJECT, ARRAY, ATOM> {
  OBJECT newObjectNode();

  ARRAY newArrayNode();

  ATOM newAtomNode(Object value);

  OBJECT readObjectNode(String resourceName);

  OBJECT translate(ModelSpec.Dictionary dictionary);

  ARRAY translate(ModelSpec.Array array);

  ATOM translate(ModelSpec.Atom atom);

  boolean hasInheritanceDirective(OBJECT child);

  OBJECT removeInheritanceDirective(OBJECT object);

  class Json implements HostLanguage<ObjectNode, ArrayNode, JsonNode> {

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
      if (value instanceof Number)
        return JsonNodeFactory.instance.numberNode((BigDecimal) value);
      if (value instanceof String)
        return JsonNodeFactory.instance.textNode((String) value);
      throw new RuntimeException(format("Unsupported value was given: '%s'", value));
    }

    @Override
    public ObjectNode readObjectNode(String resourceName) {
      return checkObjectNode(JsonUtils.readJsonNodeFromStream(ReflectionUtils.openResourceAsStream(resourceName)));
    }

    @Override
    public boolean hasInheritanceDirective(ObjectNode child) {
      return child.has(EXTENDS_KEYWORD);
    }

    @Override
    public ObjectNode removeInheritanceDirective(ObjectNode ret) {
      ret.remove(EXTENDS_KEYWORD);
      return ret;
    }

    @Override
    public ObjectNode translate(ModelSpec.Dictionary dictionary) {
      return null;
    }

    @Override
    public ArrayNode translate(ModelSpec.Array array) {
      return null;
    }

    @Override
    public JsonNode translate(ModelSpec.Atom atom) {
      return null;
    }
  }
}
