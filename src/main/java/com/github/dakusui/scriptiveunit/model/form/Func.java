package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.session.Stage;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public interface Func<O> extends Form<O> {
  List<Form> parameters();

  default O apply(Stage input) {
    return call(input).get();
  }

  default Call<O> call(Stage input) {
    return new Call<>(
        id(),
        this.body(),
        parameters().stream().map(
            param -> param.apply(input)
        ).toArray()
    );
  }

  String id();

  Function<Object[], O> body();

  class Call<O> implements Supplier<O> {
    final String                id;
    final Function<Object[], O> func;
    final Object[]              args;

    Call(String id, Function<Object[], O> func, Object[] args) {
      this.id = id;
      this.func = requireNonNull(func);
      this.args = requireNonNull(args);
    }

    @Override
    public int hashCode() {
      return this.id.hashCode();
    }

    @Override
    public boolean equals(Object anotherObject) {
      if (anotherObject instanceof Call) {
        Call another = (Call) anotherObject;
        return Objects.equals(this.id, another.id) && Arrays.equals(this.args, another.args);
      }
      return false;
    }

    @Override
    public O get() {
      return this.func.apply(this.args);
    }
  }

  final class Builder<O> {
    private final String                id;
    private final List<Form>            parameters;
    private       Function<Object[], O> body;

    public Builder(String id) {
      this.id = requireNonNull(id);
      this.parameters = new LinkedList<>();
    }

    public Builder<O> addParameter(Form param) {
      this.parameters.add(requireNonNull(param));
      return this;
    }

    @SuppressWarnings("ConstantConditions")
    public Builder<O> addParameters(Form... params) {
      Builder<O> ret = this;
      for (Form param : params)
        ret = ret.addParameter(requireNonNull(param));
      return ret;
    }

    public Builder<O> func(Function<Object[], O> body) {
      this.body = requireNonNull(body);
      return this;
    }

    public Func<O> build() {
      return new Func<O>() {
        @Override
        public List<Form> parameters() {
          return parameters;
        }

        @Override
        public String id() {
          return id;
        }

        @Override
        public Function<Object[], O> body() {
          return body;
        }

        @Override
        public String toString() {
          return id;
        }
      };
    }
  }

  static <T> Func<T> memoize(Func<T> func, Map<Call, Object> memo) {
    return new Func<T>() {
      @Override
      public List<Form> parameters() {
        return func.parameters();
      }

      @Override
      public Function<Object[], T> body() {
        return func.body();
      }

      @SuppressWarnings("unchecked")
      public T apply(Stage input) {
        Call<T> call = call(input);
        return (T) memo.computeIfAbsent(call, Call::get);
      }

      @Override
      public String id() {
        return func.id();
      }
    };
  }
}