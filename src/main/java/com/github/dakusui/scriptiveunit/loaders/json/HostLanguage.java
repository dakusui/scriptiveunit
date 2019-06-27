package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.loaders.Preprocessor;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.loaders.json.JsonPreprocessorUtils.checkObjectNode;
import static java.lang.String.format;

public interface HostLanguage<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
  NODE preprocess(JsonNode inputNode, Preprocessor preprocessor);

  OBJECT newObjectNode();

  ARRAY newArrayNode();

  ATOM newAtomNode(Object value);

  OBJECT readObjectNode(String resourceName);

  <V> V mapObjectNode(OBJECT rootNode, Class<V> valueType);

  boolean hasInheritanceDirective(OBJECT child);

  List<String> getParents(ObjectNode child);

  OBJECT removeInheritanceDirective(OBJECT object);

  OBJECT deepMerge(OBJECT work, OBJECT base);

  OBJECT translate(ModelSpec.Dictionary dictionary);

  ARRAY translate(ModelSpec.Array array);

  ATOM translate(ModelSpec.Atom atom);

  class Json implements HostLanguage<JsonNode, ObjectNode, ArrayNode, JsonNode> {
    public static final String EXTENDS_KEYWORD = "$extends";

    @Override
    public JsonNode preprocess(JsonNode inputNode, Preprocessor preprocessor) {
      // TODO
      return JsonPreprocessorUtils.translate((JsonPreprocessor) preprocessor, inputNode);
    }

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

    public <V> V mapObjectNode(ObjectNode rootNode, Class<V> valueType) {
      try {
        return new ObjectMapper().readValue(
            rootNode,
            valueType);
      } catch (IOException e) {
        throw wrap(e);
      }
    }

    @Override
    public boolean hasInheritanceDirective(ObjectNode child) {
      return child.has(EXTENDS_KEYWORD);
    }

    @Override
    public List<String> getParents(ObjectNode child) {
      return JsonPreprocessorUtils.getParentsOf(child, EXTENDS_KEYWORD);
    }

    @Override
    public ObjectNode removeInheritanceDirective(ObjectNode ret) {
      ret.remove(EXTENDS_KEYWORD);
      return ret;
    }

    @Override
    public ObjectNode deepMerge(ObjectNode work, ObjectNode base) {
      return JsonUtils.deepMerge(work, base);
    }

    @Override
    public ObjectNode translate(ModelSpec.Dictionary dictionary) {
      ObjectNode ret = newObjectNode();
      dictionary.streamKeys()
          .forEach((String eachKey) -> {
            ModelSpec.Node nodeValue = dictionary.valueOf(eachKey);
            JsonNode jsonNodeValue = translate(nodeValue);
            ret.put(eachKey, jsonNodeValue);
          });
      return ret;
    }

    JsonNode translate(ModelSpec.Node nodeValue) {
      JsonNode jsonNodeValue;
      if (nodeValue instanceof ModelSpec.Atom)
        jsonNodeValue = translate((ModelSpec.Atom) nodeValue);
      else if (nodeValue instanceof ModelSpec.Array)
        jsonNodeValue = translate((ModelSpec.Array) nodeValue);
      else if (nodeValue instanceof ModelSpec.Dictionary)
        jsonNodeValue = translate((ModelSpec.Dictionary) nodeValue);
      else
        throw new RuntimeException(format("Unsupported value was given: '%s'", nodeValue));
      return jsonNodeValue;
    }

    @Override
    public ArrayNode translate(ModelSpec.Array array) {
      ArrayNode ret = newArrayNode();
      array.stream().forEach(eachNode -> ret.add(translate(eachNode)));
      return ret;
    }

    @Override
    public JsonNode translate(ModelSpec.Atom atom) {
      return newAtomNode(atom.get());
    }
  }
}
