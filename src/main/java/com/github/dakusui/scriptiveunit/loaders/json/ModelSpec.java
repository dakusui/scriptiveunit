package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.loaders.Preprocessor;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.scriptiveunit.utils.Checks.check;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public interface ModelSpec<NODE> {
  Dictionary createDefaultValues();

  <OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> List<Preprocessor<NODE>> preprocessors(HostLanguage<NODE, OBJECT, ARRAY, ATOM> hostLanguage);

  class Standard<N> implements ModelSpec<N> {
    @Override
    public Dictionary createDefaultValues() {
      return dict(
          $("factorSpace", dict(
              $("factors", dict()),
              $("constraints", array())
          )),
          $("runnerType", atom("groupByTestOracle")),
          $("define", dict()),
          $("setUpBeforeAll", atom(null)),
          $("setUp", atom(null)),
          $("tearDown", atom(null)),
          $("tearDownAfterAll", atom(null))
      );
    }

    @Override
    public <OBJECT extends N, ARRAY extends N, ATOM extends N> List<Preprocessor<N>> preprocessors(HostLanguage<N, OBJECT, ARRAY, ATOM> hostLanguage) {
      return singletonList(hostLanguage.preprocessor(
          this,
          toUniformedObjectNodeTranslator(),
          Preprocessor.Utils.pathMatcher("factorSpace", "factors", ".*")
      ));
    }

    static <NODE, O extends NODE, A extends NODE, ATOM extends NODE> BiFunction<NODE, HostLanguage<NODE, O, A, ATOM>, NODE> toUniformedObjectNodeTranslator() {
      return (targetElement, hostLanguage) -> {
        if (hostLanguage.isObjectNode(targetElement))
          return targetElement;
        O ret = hostLanguage.newObjectNode();
        hostLanguage.putToObject(ret, "type", ((HostLanguage<NODE, O, A, ? extends NODE>) hostLanguage).newAtomNode("simple"));
        hostLanguage.putToObject(ret, "args", targetElement);
        return ret;
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
