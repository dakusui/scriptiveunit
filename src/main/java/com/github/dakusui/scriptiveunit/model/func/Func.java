package com.github.dakusui.scriptiveunit.model.func;

import com.github.dakusui.scriptiveunit.model.Stage;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface Func<O> extends Form<O> {
  List<Func> parameters();

  class Builder<O> {
    private final String name;
    private final List<Func> parameters;
    private Function<Object[], O> body;

    public Builder(String name) {
      this.name = requireNonNull(name);
      this.parameters = new LinkedList<>();
    }

    public Builder<O> addParameter(Func param) {
      this.parameters.add(requireNonNull(param));
      return this;
    }

    public Builder<O> addParameters(Func... params) {
      for (Func param : params)
        this.parameters.add(requireNonNull(param));
      return this;
    }

    public Builder<O> func(Function<Object[], O> body) {
      this.body = requireNonNull(body);
      return this;
    }

    public Func<O> build() {
      return new Func<O>() {
        @Override
        public O apply(Stage input) {
          return requireNonNull(body).apply(
              parameters().stream().map(
                  param -> param.apply(input)
              ).toArray()
          );
        }

        public List<Func> parameters() {
          return parameters;
        }

        @Override
        public String toString() {
          return name;
        }
      };
    }
  }

  interface Memoized<O> extends Form<O> {
  }
}
