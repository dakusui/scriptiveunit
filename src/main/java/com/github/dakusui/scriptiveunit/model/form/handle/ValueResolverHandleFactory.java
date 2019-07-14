package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.model.form.Value;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.model.statement.StatementRegistry;

import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class ValueResolverHandleFactory {
  private final StatementRegistry     statementRegistryForUserValueHandlers;
  private final ValueResolverRegistry valueResolverRegistry;

  public ValueResolverHandleFactory(ValueResolverRegistry valueResolverRegistry, StatementRegistry statementRegistryForUserValueHandlers) {
    this.valueResolverRegistry = valueResolverRegistry;
    this.statementRegistryForUserValueHandlers = requireNonNull(statementRegistryForUserValueHandlers);
  }

  public ValueResolverHandle create(String name) {
    return new ValueResolverHandle() {
      ValueResolverHandle valueResolverHandle = createLambdaValueResolverHandle(name)
          .orElseGet(() -> ValueResolverHandleFactory.this.createUserDefinedValueResolverHandle(name)
              .orElseGet(() -> ValueResolverHandleFactory.this.createMethodBasedValueResolverHandle(name)
                  .orElseThrow(undefinedValueResolver(name))));

      @Override
      public <U> Value<U> toValue(Statement.Compound statement) {
        Value<U> value = valueResolverHandle.toValue(statement);
        return stage -> Stage.evaluateValue(stage, value, Value::apply);
      }

      @Override
      public boolean isAccessor() {
        return valueResolverHandle.isAccessor();
      }

      @Override
      public String toString() {
        return valueResolverHandle.toString();
      }
    };
  }

  private static Supplier<UnsupportedOperationException> undefinedValueResolver(String name) {
    return () -> new UnsupportedOperationException(format("Undefined form '%s' was referenced.", name));
  }

  private Optional<ValueResolverHandle> createLambdaValueResolverHandle(String name) {
    return "lambda".equals(name) ?
        Optional.of(new ValueResolverHandle.Lambda()) :
        Optional.empty();
  }

  private Optional<ValueResolverHandle> createMethodBasedValueResolverHandle(String name) {
    return this.getObjectMethodFromDriver(name)
        .map(ValueResolverHandle.MethodBased::new);
  }

  private Optional<ValueResolverHandle> createUserDefinedValueResolverHandle(String name) {
    return getUserDefinedStatementByName(name)
        .map(ValueResolverHandle.User::new);
  }

  private Optional<Statement> getUserDefinedStatementByName(String name) {
    return this.statementRegistryForUserValueHandlers.lookUp(name);
  }

  private Optional<ValueResolver> getObjectMethodFromDriver(String methodName) {
    return this.valueResolverRegistry.lookUp(methodName);
  }
}
