package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.utils.CoreUtils;

import java.util.stream.Stream;

import static com.github.dakusui.scriptiveunit.model.session.Stage.Factory.createWrappedStage;
import static com.github.dakusui.scriptiveunit.utils.CoreUtils.iterableToStream;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.toArray;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface FormHandle {
  <U> Value<U> toValue(Statement.Compound statement);

  boolean isAccessor();

  interface Default extends FormHandle {
    @Override
    default boolean isAccessor() {
      return false;
    }
  }

  abstract class Base implements FormHandle.Default {
    @Override
    public abstract String toString();
  }

  class MethodBased extends Base {
    final Form form;

    MethodBased(Form form) {
      this.form = form;
    }

    static <U> Value<U> methodBasedFormToValue(MethodBased formHandle, Statement.Compound compound) {
      Form form = formHandle.form();
      return form.resolveValue(
          iterableToStream(compound.getArguments())
              .map(Statement::toValue)
              .toArray(Value[]::new));
    }

    @Override
    public <U> Value<U> toValue(Statement.Compound compound) {
      return Value.Named.create(
          this.form.getName(),
          methodBasedFormToValue(this, compound));
    }

    @Override
    public boolean isAccessor() {
      return this.form.isAccessor();
    }

    @Override
    public String toString() {
      return this.form.getName();
    }

    Form form() {
      return this.form;
    }
  }

  class Lambda extends Base {
    Lambda() {
    }

    @Override
    public <U> Value<U> toValue(Statement.Compound statement) {
      return Value.Named.create(
          "<lambda>",
          lambdaFormToValue(statement)
      );
    }

    @SuppressWarnings("unchecked")
    static <U> Value<U> lambdaFormToValue(Statement.Compound compound) {
      return (Value<U>) (Value<Value<Object>>) (Stage ii) ->
          getOnlyElement(iterableToStream(compound.getArguments())
              .map(Statement::toValue)
              .collect(toList()));
    }

    @Override
    public String toString() {
      return "lambda";
    }
  }

  class User extends Base {
    final Statement userDefinedStatement;

    User(Statement userDefinedStatement) {
      this.userDefinedStatement = requireNonNull(userDefinedStatement);
    }

    @Override
    public <U> Value<U> toValue(Statement.Compound statement) {
      return resolveValue(this, statement);
    }

    @SuppressWarnings("unchecked")
    private static <U> Value<U> resolveValue(User formHandle, Statement.Compound compound) {
      return (Value<U>) createUserFunc(toArray(
          Stream.concat(
              Stream.of((Value<Statement>) input -> formHandle.statement()),
              iterableToStream(compound.getArguments()).map(Statement::toValue))
              .collect(toList()),
          Value.class));
    }

    private Statement statement() {
      return this.userDefinedStatement;
    }

    @SuppressWarnings("unchecked")
    private static Value<Object> createUserFunc(Value[] args) {
      return Value.Named.create("<user>", userFunc(CoreUtils.car(args), CoreUtils.cdr(args)));
    }

    private static Value<Object> userFunc(Value<Statement> statementValue, Value<?>... args) {
      return input -> statementValue
          .apply(input)
          .toValue()
          .apply(createWrappedStage(input, args));
    }

    @Override
    public String toString() {
      return "<user>:" + userDefinedStatement;
    }
  }
}
