package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.session.Stage;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

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

  String id();

  Function<Object[], O> body();

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
