package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.model.statement.StatementRegistry;

import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class FormHandleFactory {
  private final StatementRegistry statementRegistryForUserStatements;
  private final FormRegistry      formRegistry;

  public FormHandleFactory(FormRegistry formRegistry, StatementRegistry statementRegistryForUserStatements) {
    this.formRegistry = formRegistry;
    this.statementRegistryForUserStatements = requireNonNull(statementRegistryForUserStatements);
  }

  public FormHandle create(String name) {
    return new FormHandle() {
      FormHandle formHandle = createLambdaFormHandle(name)
          .orElseGet(() -> FormHandleFactory.this.createUserDefinedFormHandle(name)
              .orElseGet(() -> FormHandleFactory.this.createMethodBasedFormHandle(name)
                  .orElseThrow(undefinedForm(name))));

      @Override
      public <U> Value<U> toValue(Statement.Compound statement) {
        Value<U> value = formHandle.toValue(statement);
        return Value.Named.create(Statement.format(statement), stage -> Stage.evaluateValue(stage, value, Value::apply));
      }

      @Override
      public boolean isAccessor() {
        return formHandle.isAccessor();
      }

      @Override
      public String toString() {
        return formHandle.toString();
      }
    };
  }

  private static Supplier<UnsupportedOperationException> undefinedForm(String name) {
    return () -> new UnsupportedOperationException(format("Undefined form '%s' was referenced.", name));
  }

  private Optional<FormHandle> createLambdaFormHandle(String name) {
    return "lambda".equals(name) ?
        Optional.of(new FormHandle.Lambda()) :
        Optional.empty();
  }

  private Optional<FormHandle> createMethodBasedFormHandle(String name) {
    return this.getObjectMethodFromDriver(name)
        .map(FormHandle.MethodBased::new);
  }

  private Optional<FormHandle> createUserDefinedFormHandle(String name) {
    return getUserDefinedStatementByName(name)
        .map(FormHandle.User::new);
  }

  private Optional<Statement> getUserDefinedStatementByName(String name) {
    return this.statementRegistryForUserStatements.lookUp(name);
  }

  private Optional<Form> getObjectMethodFromDriver(String methodName) {
    return this.formRegistry.lookUp(methodName);
  }
}
