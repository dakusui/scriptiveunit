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

public interface ValueResolverHandle {
  <U> Value<U> toValue(Statement.Compound statement);

  boolean isAccessor();

  interface Default extends ValueResolverHandle {
    @Override
    default boolean isAccessor() {
      return false;
    }
  }

  abstract class Base implements ValueResolverHandle.Default {
    @Override
    public abstract String toString();
  }

  class MethodBased extends Base {
    final ValueResolver valueResolver;

    MethodBased(ValueResolver valueResolver) {
      this.valueResolver = valueResolver;
    }

    static <U> Value<U> methodBasedValueResolverToValue(MethodBased formHandle, Statement.Compound compound) {
      ValueResolver valueResolver = formHandle.valueResolver();
      return valueResolver.resolveValue(
          iterableToStream(compound.getArguments())
              .map(Statement::toValue)
              .toArray(Value[]::new));
    }

    @Override
    public <U> Value<U> toValue(Statement.Compound compound) {
      return Value.Named.create(
          this.valueResolver.getName(),
          methodBasedValueResolverToValue(this, compound));
    }

    @Override
    public boolean isAccessor() {
      return this.valueResolver.isAccessor();
    }

    @Override
    public String toString() {
      return this.valueResolver.getName();
    }

    ValueResolver valueResolver() {
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
          lambdaValueResolverToValue(statement)
      );
    }

    static <U> Value<U> lambdaValueResolverToValue(Statement.Compound compound) {
      //noinspection unchecked
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

    private static <U> Value<U> resolveValue(User formHandle, Statement.Compound compound) {
      //noinspection unchecked
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
