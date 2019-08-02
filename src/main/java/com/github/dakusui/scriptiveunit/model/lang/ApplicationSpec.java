package com.github.dakusui.scriptiveunit.model.lang;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitUnclassifiedException;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.PreprocessingUnit;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.scriptiveunit.exceptions.Requirements.isInstanceOf;
import static com.github.dakusui.scriptiveunit.exceptions.Requirements.require;
import static com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.Utils.requireDictionary;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface ApplicationSpec {
  Dictionary createDefaultValues();

  List<PreprocessingUnit> preprocessorUnits();

  Dictionary removeInheritanceDirective(Dictionary inputNode);

  List<String> parentsOf(Dictionary rootNode);

  default Dictionary deepMerge(Dictionary source, Dictionary target) {
    requireNonNull(source);
    requireNonNull(target);
    return dict(Stream.concat(
        target.streamKeys()
            .map(each -> source.containsKey(each) ?
                isDictionary(source.valueOf(each)) ?
                    $(each, deepMerge(
                        requireDictionary(source.valueOf(each)),
                        requireDictionary(target.valueOf(each)))) :
                    $(each, source.valueOf(each)) :
                $(each, target.valueOf(each))),
        source.streamKeys()
            .filter(each -> !target.containsKey(each))
            .map(each -> $(each, source.valueOf(each))))
        .toArray(Dictionary.Entry[]::new));
  }

  static ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary inputNode, PreprocessingUnit preprocessingUnit) {
    return (Dictionary) preprocess(preprocessingUnit, PreprocessingUnit.Path.createRoot(), inputNode);
  }

  static Node preprocess(PreprocessingUnit preprocessingUnit, PreprocessingUnit.Path pathToTarget, Node targetElement) {
    if (preprocessingUnit.matches(pathToTarget)) {
      return preprocessingUnit.translate(targetElement);
    }
    Node work;
    if (isDictionary(targetElement)) {
      work = dict(((Dictionary) targetElement).streamKeys().map(
          (String attributeName) -> $(
              attributeName,
              preprocess(
                  preprocessingUnit,
                  pathToTarget.createChild(attributeName),
                  ((Dictionary) targetElement).valueOf(attributeName)))).toArray(Dictionary.Entry[]::new));
    } else if (isArray(targetElement)) {
      AtomicInteger i = new AtomicInteger(0);
      work = array(((Array) targetElement)
          .stream()
          .map((Node each) -> preprocess(
              preprocessingUnit,
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

  static boolean isDictionary(ApplicationSpec.Node node) {
    return node instanceof Dictionary;
  }

  static boolean isArray(ApplicationSpec.Node node) {
    return node instanceof Array;
  }

  static boolean isAtom(ApplicationSpec.Node node) {
    return node instanceof Atom;
  }

  enum Utils {
    ;

    public static Dictionary requireDictionary(Node node) {
      return (Dictionary) require(node, isInstanceOf(Dictionary.class), ScriptiveUnitUnclassifiedException::new);
    }

    private static String requireString(Object object) {
      return (String) require(object, isInstanceOf(String.class), ScriptiveUnitUnclassifiedException::new);
    }
  }

  abstract class Base implements ApplicationSpec {
    @Override
    public List<String> parentsOf(Dictionary rootNode) {
      return rootNode.containsKey(inheritanceKeyword()) ?
          toStringList(requireArray(rootNode.valueOf(inheritanceKeyword()))) :
          emptyList();
    }

    private String inheritanceKeyword() {
      return HostSpec.Json.EXTENDS_KEYWORD;
    }

    private static Array requireArray(Node node) {
      return (Array) require(node, isInstanceOf(Array.class), IllegalArgumentException::new);
    }

    private static Atom requireAtom(Node node) {
      return (Atom) require(node, isInstanceOf(Atom.class), IllegalArgumentException::new);
    }

    @Override
    public Dictionary removeInheritanceDirective(Dictionary inputNode) {
      return ApplicationSpec.dict(
          inputNode.streamKeys()
              .filter(each -> !Objects.equals(inheritanceKeyword(), each))
              .map(each -> $(each, inputNode.valueOf(each)))
              .toArray(Dictionary.Entry[]::new)
      );
    }

    private static List<String> toStringList(Array array) {
      return array.stream()
          .map(Base::requireAtom)
          .map(Atom::get)
          .map(Utils::requireString)
          .collect(toList());
    }
  }

  class Standard extends Base {
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
    public List<PreprocessingUnit> preprocessorUnits() {
      return singletonList(
          PreprocessingUnit.preprocessor(toUniformedObjectNodeTranslator(),
              PreprocessingUnit.Utils.pathMatcher("factorSpace", "factors", ".*")));
    }

    static Function<Node, Node> toUniformedObjectNodeTranslator() {
      return (targetElement) -> {
        if (isDictionary(targetElement))
          return targetElement;
        return dict(
            $("type", atom("simple")),
            $("args", targetElement));
      };
    }
  }


  static Atom atom(Object value) {
    check(value, v -> !(v instanceof Node),
        "Value must not be an instance of '%s' but was: %s", Node.class.getSimpleName(), value);
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

  static Atom $(String key) {
    return atom(key);
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

    interface Factory {
      static Dictionary emptyDictionary() {
        return new Factory() {
        }.dict();
      }

      default Dictionary dict(Entry... entries) {
        return ApplicationSpec.dict(entries);
      }

      default Array array(Object... values) {
        return ApplicationSpec.array((Node[])
            Arrays.stream(values)
                .map(each -> each instanceof Node ?
                    ((Node) each) :
                    ApplicationSpec.atom(each))
                .toArray(Node[]::new));
      }

      default Entry entry(String key, Object value) {
        return ApplicationSpec.entry(key,
            value instanceof Node ?
                (Node) value :
                atom(value));
      }

      default Entry $(String key, Object value) {
        return this.entry(key, value);
      }
    }
  }
}
