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
  NODE preprocess(OBJECT inputNode, Preprocessor preprocessor);

  OBJECT newObjectNode();

  ARRAY newArrayNode();

  ATOM newAtomNode(Object value);

  OBJECT readObjectNode(String resourceName);

  <V> V mapObjectNode(OBJECT rootNode, Class<V> valueType);

  boolean hasInheritanceDirective(OBJECT child);

  List<String> getParents(OBJECT child);

  OBJECT removeInheritanceDirective(OBJECT object);

  OBJECT deepMerge(OBJECT work, OBJECT base);

  default OBJECT translate(ModelSpec.Dictionary dictionary) {
    OBJECT ret = newObjectNode();
    dictionary.streamKeys()
        .forEach((String eachKey) -> {
          ModelSpec.Node nodeValue = dictionary.valueOf(eachKey);
          NODE jsonNodeValue = translate(nodeValue);
          putToObject(ret, eachKey, jsonNodeValue);
        });
    return ret;
  }

  default ARRAY translate(ModelSpec.Array array) {
    ARRAY ret = newArrayNode();
    array.stream().forEach(eachNode -> addToArray(ret, eachNode));
    return ret;
  }

  default ATOM translate(ModelSpec.Atom atom) {
    return newAtomNode(atom.get());
  }

  default NODE translate(ModelSpec.Node modelNode) {
    NODE nodeValue;
    if (isAtom(modelNode))
      nodeValue = translate((ModelSpec.Atom) modelNode);
    else if (isArray(modelNode))
      nodeValue = translate((ModelSpec.Array) modelNode);
    else if (isDictionary(modelNode))
      nodeValue = translate((ModelSpec.Dictionary) modelNode);
    else
      throw new RuntimeException(format("Unsupported value was given: '%s'", modelNode));
    return nodeValue;
  }

  void putToObject(OBJECT ret, String eachKey, NODE jsonNodeValue);

  void addToArray(ARRAY ret, ModelSpec.Node eachNode);


  class Json implements HostLanguage<JsonNode, ObjectNode, ArrayNode, JsonNode> {
    public static final String EXTENDS_KEYWORD = "$extends";

    @Override
    public JsonNode preprocess(ObjectNode inputNode, Preprocessor preprocessor) {
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
    public void putToObject(ObjectNode ret, String eachKey, JsonNode jsonNodeValue) {
      ret.put(eachKey, jsonNodeValue);
    }

    @Override
    public void addToArray(ArrayNode ret, ModelSpec.Node eachNode) {
      ret.add(translate(eachNode));
    }
  }

  static boolean isDictionary(ModelSpec.Node nodeValue) {
    return nodeValue instanceof ModelSpec.Dictionary;
  }

  static boolean isArray(ModelSpec.Node nodeValue) {
    return nodeValue instanceof ModelSpec.Array;
  }

  static boolean isAtom(ModelSpec.Node nodeValue) {
    return nodeValue instanceof ModelSpec.Atom;
  }
}
