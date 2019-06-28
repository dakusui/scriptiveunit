package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.model.statement.StatementRegistry;

import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class FormHandleFactory {
  private final StatementRegistry statementRegistryForUserForms;
  private final ObjectMethodRegistry objectMethodRegistry;

  public FormHandleFactory(ObjectMethodRegistry objectMethodRegistry, StatementRegistry statementRegistryForUserForms) {
    this.objectMethodRegistry = objectMethodRegistry;
    this.statementRegistryForUserForms = requireNonNull(statementRegistryForUserForms);
  }

  public FormHandle create(String name) {
    return createLambdaFormHandle(name)
        .orElseGet(() -> FormHandleFactory.this.createUserDefinedFormHandle(name)
            .orElseGet(() -> FormHandleFactory.this.createMethodBasedFormHandle(name)
                .orElseThrow(undefinedForm(name))));
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
    return this.statementRegistryForUserForms.lookUp(name);
  }

  private Optional<ObjectMethod> getObjectMethodFromDriver(String methodName) {
    return this.objectMethodRegistry.lookUp(methodName);
  }
}
