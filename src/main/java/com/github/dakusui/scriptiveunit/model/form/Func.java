package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.session.Stage;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface Func<O> extends Form<O> {
  List<Form> parameters();

  O apply(Stage input);

  Function<Object[], O> body();

  static <O> Builder<O> body(Function<Object[], O> body) {
    return new Builder<O>().func(body);
  }

  final class Builder<O> {
    private final List<Form>            parameters;
    private       Function<Object[], O> body;

    public Builder() {
      this.parameters = new LinkedList<>();
    }

    public Builder<O> addParameter(Form param) {
      this.parameters.add(requireNonNull(param));
      return this;
    }

    public Builder<O> func(Function<Object[], O> body) {
      this.body = requireNonNull(body);
      return this;
    }

    public Func<O> $() {
      return build();
    }

    public Func<O> build() {
      return new Func<O>() {
        @Override
        public List<Form> parameters() {
          return parameters;
        }

        @Override
        public O apply(Stage input) {
          return body().apply(parameters()
              .stream()
              .map(param -> param.apply(input))
              .toArray());
        }

        @Override
        public Function<Object[], O> body() {
          return body;
        }
      };
    }
  }
}
