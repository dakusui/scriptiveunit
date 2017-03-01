package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.core.Preprocessor.Path;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.*;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public enum JsonUtils {
  ;
  public static NullNode NULL = NullNode.instance;

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

  public static Preprocessor preprocessor(Function<JsonNode, JsonNode> translator, Predicate<Path> pathMatcher) {
    requireNonNull(translator);
    requireNonNull(pathMatcher);
    return new Preprocessor() {
      @Override
      public JsonNode translate(JsonNode targetElement) {
        return translator.apply(targetElement);
      }

      @Override
      public boolean matches(Path pathToTargetElement) {
        return pathMatcher.test(pathToTargetElement);
      }
    };
  }

  public static Predicate<Path> pathMatcher(String... args) {
    return new Predicate<Path>() {
      @Override
      public boolean test(Path path) {
        List<Path.Component> pathComponents = path.asComponentList();
        if (args.length != pathComponents.size())
          return false;
        int i = 0;
        for (String eachArg : args) {
          if (!pathComponents.get(i).value().toString().matches(eachArg))
            return false;
          i++;
        }
        return true;
      }

      @Override
      public String toString() {
        return stream(args).collect(toList()).toString();
      }
    };
  }
}
