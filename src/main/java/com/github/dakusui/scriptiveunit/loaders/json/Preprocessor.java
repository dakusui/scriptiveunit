package com.github.dakusui.scriptiveunit.loaders.json;

import com.google.common.collect.Lists;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface Preprocessor {
  JsonNode translate(JsonNode targetElement);

  boolean matches(Path pathToTargetElement);

  enum Utils {
    ;
    public static List<Path.Component> pathComponentList(Object... pathComponents) {
      return Lists.newArrayList(pathComponents).stream().map(Utils::pathComponent).collect(toList());
    }

    public static Path.Component pathComponent(Object value) {
      if (value instanceof Integer)
        return Path.Component.num((Integer) value);
      if (value instanceof String)
        return Path.Component.text((String) value);
      throw new RuntimeException();
    }

  }

  interface Path {

    Path createChild(String attributeName);

    Path createChild(int index);

    List<Component> asComponentList();


    interface Component<T> {
      T value();

      abstract class Base<T> implements Component<T> {
        final T value;

        Base(T value) {
          this.value = requireNonNull(value);
        }

        @Override
        public int hashCode() {
          return value().hashCode();
        }

        @Override
        public boolean equals(Object object) {
          return object instanceof Component
              && Objects.equals(this.value(), ((Component) object).value());
        }

        public T value() {
          return this.value;
        }
      }

      class Text extends Component.Base<String> {
        Text(String value) {
          super(value);
        }

        public String toString() {
          return this.value();
        }
      }

      class Num extends Component.Base<Integer> {

        Num(int value) {
          super(value);
        }

        public String toString() {
          return value().toString();
        }
      }

      static Component text(String text) {
        return new Text(text);
      }

      static Component num(int i) {
        return new Num(i);
      }
    }

    class Impl implements Path {
      private final List<Component> components;

      Impl(List<Component> components) {
        this.components = unmodifiableList(components);
      }

      @Override
      public Path createChild(String attributeName) {
        return new Impl(append(
            this.components,
            new Component.Text(attributeName)
        ));
      }

      @Override
      public Path createChild(int index) {
        return new Impl(append(
            this.components,
            new Component.Num(index)
        ));
      }

      static private <T> List<T> append(List<T> list, T v) {
        return newLinkedList(concat(
            list,
            singletonList(v)
        ));
      }

      @Override
      public List<Component> asComponentList() {
        return this.components;
      }
    }

    static Path createRoot() {
      return new Impl(Collections.emptyList());
    }
  }

  static JsonNode translate(Preprocessor preprocessor, JsonNode rootNode) {
    return translate(preprocessor, Path.createRoot(), rootNode);
  }

  static JsonNode translate(Preprocessor preprocessor, Path pathToTarget, JsonNode targetElement) {
    if (preprocessor.matches(pathToTarget)) {
      return preprocessor.translate(targetElement);
    }
    JsonNode work;
    if (targetElement instanceof ObjectNode) {
      work = targetElement;
      ((Iterable<String>) targetElement::getFieldNames).forEach(
          (String attributeName) ->
              ((ObjectNode) targetElement).put(
                  attributeName,
                  translate(preprocessor, pathToTarget.createChild(attributeName), targetElement.get(attributeName))
              ));
    } else if (targetElement instanceof ArrayNode) {
      AtomicInteger i = new AtomicInteger(0);
      work = new ArrayNode(JsonNodeFactory.instance);
      targetElement.forEach(
          (JsonNode jsonNode) -> ((ArrayNode) work).add(
              translate(
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
}
