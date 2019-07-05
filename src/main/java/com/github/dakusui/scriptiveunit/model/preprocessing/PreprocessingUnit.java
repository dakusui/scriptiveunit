package com.github.dakusui.scriptiveunit.model.preprocessing;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface PreprocessingUnit {
  ApplicationSpec.Node translate(ApplicationSpec.Node targetElement);

  boolean matches(PreprocessingUnit.Path pathToTargetElement);

  /**
   * Returns a preprocessor instance which translates a node on a path specified by {@code pathMatcher}
   * using a function {@code translator}.
   *
   * @param translator  A function with which the translation is made.
   * @param pathMatcher A predicate that returns {@code true} for a path in a JSON node,
   *                    where trcommanslations by {@code translator} are desired.
   * @return A new preprocessor.
   */
  static PreprocessingUnit preprocessor(Function<ApplicationSpec.Node, ApplicationSpec.Node> translator, Predicate<Path> pathMatcher) {
    requireNonNull(translator);
    requireNonNull(pathMatcher);
    return new PreprocessingUnit() {
      @Override
      public ApplicationSpec.Node translate(ApplicationSpec.Node targetElement) {
        return translator.apply(targetElement);
      }

      @Override
      public boolean matches(Path pathToTargetElement) {
        return pathMatcher.test(pathToTargetElement);
      }
    };
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

      @Override
      public List<Component> asComponentList() {
        return this.components;
      }

      static private <T> List<T> append(List<T> list, T v) {
        return new ArrayList<T>(list.size() + 1) {{
          addAll(list);
          add(v);
        }};
      }
    }

    static Path createRoot() {
      return new Impl(Collections.emptyList());
    }
  }

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
}
