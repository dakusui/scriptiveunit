package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.session.Stage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public interface Func<O> extends Form<O> {
  /**
   * This method returns an identifier string for a {@link Func} object.
   * Note that the ID is composed by FQCN of the class and method name that directly calls this method by calling
   * {@link Thread#currentThread()}{@code .getStackTrace()}.
   * This means, refactoring, such as "extract method", made on the caller method
   * may result in an unintended behaviour change and you need to be cautious.
   */
  static String funcId() {
    StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
    return String.format("%s:%s", caller.getClassName(), caller.getMethodName());
  }

  static <T> Func<T> createFunc(String id, Function<Object[], T> body, Func<?>... parameters) {
    return new Builder<T>(id)
        .func(body)
        .addParameters(parameters)
        .build();
  }

  List<Form> parameters();

  O apply(Stage input);

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
    private       boolean               memoized = false;

    public Builder(String id) {
      this.id = requireNonNull(id);
      this.parameters = new LinkedList<>();
    }

    public Builder<O> addParameter(Form param) {
      this.parameters.add(requireNonNull(param));
      return this;
    }

    public Builder<O> memoize() {
      this.memoized = true;
      return this;
    }

    @SuppressWarnings("ConstantConditions")
    Builder<O> addParameters(Form... params) {
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
      Function<Object[], O> body = memoize(this.body);
      return new Func<O>() {
        @Override
        public List<Form> parameters() {
          return parameters;
        }

        @Override
        public O apply(Stage input) {
          return call(input).get();
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

    private static <O> Function<Object[], O> memoize(Function<Object[], O> body) {
      return new Function<Object[], O>() {
        Map<List<Object>, O> memo = new HashMap<>();

        @Override
        public O apply(Object[] objects) {
          return memo.computeIfAbsent(asList(objects), args -> body.apply(args.toArray()));
        }
      };
    }
  }
}
