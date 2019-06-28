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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.loaders.json.JsonPreprocessorUtils.requireObjectNode;
import static java.lang.String.format;

public interface HostLanguage<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
  Preprocessor<NODE> preprocessor(
      ModelSpec<NODE> modelSpec,
      BiFunction<NODE, HostLanguage<NODE, OBJECT, ARRAY, ATOM>, NODE> translator,
      Predicate<Preprocessor.Path> pathMatcher);

  OBJECT preprocess(OBJECT inputNode, Preprocessor<NODE> preprocessor);

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
    array.stream().forEach((ModelSpec.Node eachNode) -> addToArray(ret, translate(eachNode)));
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

  @SuppressWarnings("unchecked")
  default NODE preprocess_(Preprocessor<NODE> preprocessor, Preprocessor.Path pathToTarget, NODE targetElement) {
    if (preprocessor.matches(pathToTarget)) {
      return preprocessor.translate(targetElement);
    }
    NODE work;
    if (isObjectNode(targetElement)) {
      work = targetElement;
      keysOf((OBJECT) targetElement).forEach(
          attributeName -> putToObject(
              (OBJECT) work,
              attributeName,
              this.preprocess_(
                  preprocessor,
                  pathToTarget.createChild(attributeName),
                  valueOf((OBJECT) targetElement, attributeName))));
    } else if (isArrayNode(targetElement)) {
      AtomicInteger i = new AtomicInteger(0);
      work = newArrayNode();
      elementsOf((ARRAY) targetElement).forEach(
          (NODE jsonNode) -> addToArray((ARRAY) work,
              preprocess_(
                  preprocessor,
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

  void putToObject(OBJECT ret, String eachKey, NODE jsonNodeValue);

  void addToArray(ARRAY ret, NODE eachNode);

  default ModelSpec.Dictionary toDictionary(OBJECT object) {
    return ModelSpec.dict(
        keysOf(object)
            .map(k -> ModelSpec.$(k, toModelNode(valueOf(object, k)))).toArray(ModelSpec.Dictionary.Entry[]::new)
    );
  }

  default ModelSpec.Array toArray(ARRAY array) {
    return ModelSpec.array(elementsOf(array)
        .map(this::toModelNode)
        .toArray(ModelSpec.Node[]::new));
  }

  default ModelSpec.Atom toAtom(ATOM atom) {
    return ModelSpec.atom(valueOf(atom));
  }

  @SuppressWarnings("unchecked")
  default ModelSpec.Node toModelNode(NODE node) {
    if (isAtomNode(node))
      return toAtom((ATOM) node);
    if (isArrayNode(node))
      return toArray((ARRAY) node);
    if (isObjectNode(node))
      return toDictionary((OBJECT) node);
    throw new UnsupportedOperationException();
  }

  class Json implements HostLanguage<JsonNode, ObjectNode, ArrayNode, JsonNode> {
    public static final String EXTENDS_KEYWORD = "$extends";

    @Override
    public Preprocessor<JsonNode> preprocessor(
        ModelSpec<JsonNode> modelSpec, BiFunction<JsonNode, HostLanguage<JsonNode, ObjectNode, ArrayNode, JsonNode>, JsonNode> translator,
        Predicate<Preprocessor.Path> pathMatcher) {
      return Preprocessor.preprocessor(jsonNode -> translator.apply(jsonNode, Json.this), pathMatcher);
    }

    @Override
    public ObjectNode preprocess(ObjectNode inputNode, Preprocessor<JsonNode> preprocessor) {
      return (ObjectNode) preprocess_(preprocessor, Preprocessor.Path.createRoot(), inputNode);
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
      throw new UnsupportedOperationException();
    }

    @Override
    public ObjectNode readObjectNode(String resourceName) {
      return requireObjectNode(JsonUtils.readJsonNodeFromStream(ReflectionUtils.openResourceAsStream(resourceName)));
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
    public void addToArray(ArrayNode ret, JsonNode eachNode) {
      ret.add(eachNode);
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
