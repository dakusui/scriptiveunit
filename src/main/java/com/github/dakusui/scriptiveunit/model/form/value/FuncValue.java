package com.github.dakusui.scriptiveunit.model.form.value;

import com.github.dakusui.scriptiveunit.model.session.Stage;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface FuncValue<O> extends Value<O> {
  List<Value> parameters();

  O apply(Stage input);

  Function<Object[], O> body();

  static <O> Builder<O> body(Function<Object[], O> body) {
    return new Builder<O>().func(body);
  }

  final class Builder<O> {
    private final List<Value>           parameters;
    private       Function<Object[], O> body;

    public Builder() {
      this.parameters = new LinkedList<>();
    }

    public Builder<O> addParameter(Value param) {
      this.parameters.add(requireNonNull(param));
      return this;
    }

    public Builder<O> func(Function<Object[], O> body) {
      this.body = requireNonNull(body);
      return this;
    }

    public FuncValue<O> $() {
      return build();
    }

    public FuncValue<O> build() {
      return new FuncValue<O>() {
        @Override
        public List<Value> parameters() {
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
