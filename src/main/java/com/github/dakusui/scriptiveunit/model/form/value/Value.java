package com.github.dakusui.scriptiveunit.model.form.value;

import com.github.dakusui.scriptiveunit.model.stage.Stage;

import java.util.function.Function;

@FunctionalInterface
public interface Value<O> extends Function<Stage, O> {
  @Override
  O apply(Stage input);

  default String name() {
    return "<noname>";
  }

  interface Named<O> extends Value<O> {
    String name();

    static <O> Named<O> create(String name, Value<O> value) {
      return new Named<O>() {
        @Override
        public String name() {
          return name;
        }

        @Override
        public O apply(Stage input) {
          return value.apply(input);
        }
      };
    }
  }

  /**
   * An interface that represents a constant. The {@code apply} method of an
   * implementation of this class must return the same value always regardless
   * of its arguments value, even if a {@code null} is given.
   *
   * @param <O> Type of output constant.
   */
  interface Const<O> extends Value<O> {
  }

  interface Listener {
    void enter(Value value);

    void leave(Value form, Object value);

    void fail(Value value, Throwable t);
  }
}
