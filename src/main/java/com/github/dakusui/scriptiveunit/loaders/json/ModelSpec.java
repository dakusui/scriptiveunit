package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.Preprocessor;
import com.github.dakusui.scriptiveunit.utils.Checks;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.scriptiveunit.loaders.json.ModelSpec.Utils.nonDictionaryFound;
import static com.github.dakusui.scriptiveunit.loaders.json.ModelSpec.Utils.requireDictionary;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public interface ModelSpec {
  static ModelSpec.Dictionary preprocess(ModelSpec.Dictionary inputNode, Preprocessor preprocessor) {
    return (Dictionary) preprocess(preprocessor, Preprocessor.Path.createRoot(), inputNode);
  }

  static Node preprocess(Preprocessor preprocessor, Preprocessor.Path pathToTarget, Node targetElement) {
    if (preprocessor.matches(pathToTarget)) {
      return preprocessor.translate(targetElement);
    }
    Node work;
    if (isDictionary(targetElement)) {
      //work = targetElement;
      work = dict(((Dictionary) targetElement).streamKeys().map(
          (String attributeName) -> $(
              attributeName,
              preprocess(
                  preprocessor,
                  pathToTarget.createChild(attributeName),
                  ((Dictionary) targetElement).valueOf(attributeName)))).toArray(Dictionary.Entry[]::new));
    } else if (isArray(targetElement)) {
      AtomicInteger i = new AtomicInteger(0);
      work = array(((Array) targetElement)
          .stream()
          .map((Node each) -> preprocess(
              preprocessor,
              pathToTarget.createChild(i.getAndIncrement()),
              each
          )).toArray(Node[]::new)
      );
    } else {
      work = targetElement;
    }
    return Objects.equals(targetElement, work) ?
        targetElement :
        work;
  }

  Dictionary createDefaultValues();

  List<Preprocessor> preprocessors();

  static boolean isDictionary(ModelSpec.Node node) {
    return node instanceof Dictionary;
  }

  static boolean isArray(ModelSpec.Node node) {
    return node instanceof Array;
  }

  static boolean isAtom(ModelSpec.Node node) {
    return node instanceof Atom;
  }

  static Dictionary deepMerge(Dictionary source, Dictionary target) {
    requireNonNull(source);
    requireNonNull(target);
    return dict(Stream.concat(
        target.streamKeys()
            .map(each -> source.containsKey(each) ?
                isDictionary(source.valueOf(each)) ?
                    $(each, deepMerge(
                        requireDictionary(source.valueOf(each), nonDictionaryFound(each)),
                        requireDictionary(target.valueOf(each), nonDictionaryFound(each)))) :
                    $(each, source.valueOf(each)) :
                $(each, target.valueOf(each))),
        source.streamKeys()
            .filter(each -> !target.containsKey(each))
            .map(each -> $(each, source.valueOf(each))))
        .toArray(Dictionary.Entry[]::new));
  }

  enum Utils {
    ;

    static Function<Node, ScriptiveUnitException> nonDictionaryFound(String key) {
      return node -> {
        throw new ScriptiveUnitException(format("Non dictionary node:'%s' was found at key:'%s'", node, key));
      };
    }

    static Function<Node, ScriptiveUnitException> notDictionary() {
      return node -> {
        throw new ScriptiveUnitException(format("Node:'%s' is not a dictionary", node));
      };
    }

    public static Dictionary requireDictionary(Node node) {
      return requireDictionary(node, notDictionary());
    }

    public static Dictionary requireDictionary(Node node, Function<Node, ScriptiveUnitException> otherwiseThrow) {
      Checks.check(isDictionary(node), () -> otherwiseThrow.apply(node));
      return (Dictionary) node;
    }
  }

  class Standard implements ModelSpec {
    @Override
    public Dictionary createDefaultValues() {
      return dict(
          $("factorSpace", dict(
              $("factors", dict()),
              $("constraints", array()))),
          $("runnerType", atom("groupByTestOracle")),
          $("define", dict()),
          $("setUpBeforeAll", atom(null)),
          $("setUp", atom(null)),
          $("tearDown", atom(null)),
          $("tearDownAfterAll", atom(null)));
    }

    @Override
    public List<Preprocessor> preprocessors() {
      return singletonList(Preprocessor.preprocessor(toUniformedObjectNodeTranslator_(),
          Preprocessor.Utils.pathMatcher("factorSpace", "factors", ".*")));
    }

    static Function<Node, Node> toUniformedObjectNodeTranslator_() {
      return (targetElement) -> {
        if (isDictionary(targetElement))
          return targetElement;
        return dict(
            $("type", atom("simple")),
            $("args", targetElement)
        );
      };
    }
  }


  static Atom atom(Object value) {
    check(value, v -> !(v instanceof Node), "Value must not be an instance of '%s' but was: %s", Node.class, value);
    return new Atom() {
      @SuppressWarnings("unchecked")
      @Override
      public <T> T get() {
        return (T) value;
      }
    };
  }

  static Array array(Node... nodes) {
    return new Array() {
      @Override
      public Node get(int i) {
        return nodes[i];
      }

      @Override
      public int size() {
        return nodes.length;
      }
    };
  }

  static Dictionary dict(Dictionary.Entry... entries) {
    return new Dictionary() {
      Map<String, Node> map = new LinkedHashMap<String, Node>() {{
        for (Entry each : entries)
          put(each.key(), each.value());
      }};

      @Override
      public Iterable<String> keys() {
        return map.keySet();
      }

      @Override
      public Node valueOf(String key) {
        return map.get(key);
      }

      @Override
      public boolean containsKey(String each) {
        return map.containsKey(each);
      }

      @Override
      public int size() {
        return map.size();
      }
    };
  }

  static Dictionary.Entry entry(String key, Node value) {
    requireNonNull(key);
    requireNonNull(value);
    return new Dictionary.Entry() {
      @Override
      public String key() {
        return key;
      }

      @Override
      public Node value() {
        return value;
      }
    };
  }

  static Dictionary.Entry $(String key, Node value) {
    return entry(key, value);
  }

  interface Node {
  }

  interface Atom extends Node {
    <T> T get();
  }

  interface Array extends Node, Iterable<Node> {
    Node get(int i);

    int size();

    @SuppressWarnings("NullableProblems")
    @Override
    default Iterator<Node> iterator() {
      return new Iterator<Node>() {
        int i = 0;

        @Override
        public boolean hasNext() {
          return i < size();
        }

        @Override
        public Node next() {
          return get(i++);
        }
      };
    }

    default Stream<Node> stream() {
      return StreamSupport.stream(this.spliterator(), false);
    }
  }

  interface Dictionary extends Node {
    Iterable<String> keys();

    Node valueOf(String key);

    boolean containsKey(String each);

    int size();

    default Stream<String> streamKeys() {
      return StreamSupport.stream(keys().spliterator(), false);
    }


    interface Entry {
      String key();

      Node value();
    }
  }
}
