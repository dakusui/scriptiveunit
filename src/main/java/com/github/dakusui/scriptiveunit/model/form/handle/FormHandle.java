package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.model.form.Value;
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
    final ValueResolver valueResolver;

    MethodBased(ValueResolver valueResolver) {
      this.valueResolver = valueResolver;
    }

    static <U> Value<U> methodBasedFormHandleToForm(MethodBased formHandle, Statement.Compound compound) {
      ValueResolver valueResolver = formHandle.objectMethod();
      return valueResolver.resolveValue(
          iterableToStream(compound.getArguments())
              .map(Statement::toForm)
              .toArray(Value[]::new));
    }

    @Override
    public <U> Value<U> toValue(Statement.Compound compound) {
      return Value.Named.create(
          this.valueResolver.getName(),
          methodBasedFormHandleToForm(this, compound));
    }

    @Override
    public boolean isAccessor() {
      return this.valueResolver.isAccessor();
    }

    @Override
    public String toString() {
      return this.valueResolver.getName();
    }

    ValueResolver objectMethod() {
      return this.valueResolver;
    }
  }

  class Lambda extends Base {
    Lambda() {
    }

    @Override
    public <U> Value<U> toValue(Statement.Compound statement) {
      return Value.Named.create(
          "<lambda>",
          lambdaFormHandleToForm(statement)
      );
    }

    static <U> Value<U> lambdaFormHandleToForm(Statement.Compound compound) {
      //noinspection unchecked
      return (Value<U>) (Value<Value<Object>>) (Stage ii) ->
          getOnlyElement(iterableToStream(compound.getArguments())
              .map(Statement::toForm)
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
      return userFormHandleToForm(this, statement);
    }

    private static <U> Value<U> userFormHandleToForm(User formHandle, Statement.Compound compound) {
      //noinspection unchecked
      return (Value<U>) createUserFunc(toArray(
          Stream.concat(
              Stream.of((Value<Statement>) input -> formHandle.statement()),
              iterableToStream(compound.getArguments()).map(Statement::toForm))
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
          .toForm()
          .apply(createWrappedStage(input, args));
    }

    @Override
    public String toString() {
      return "<user>:" + userDefinedStatement;
    }
  }
}
