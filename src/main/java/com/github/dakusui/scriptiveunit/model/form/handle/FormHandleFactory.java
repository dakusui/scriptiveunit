package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.model.statement.StatementRegistry;
import com.github.dakusui.scriptiveunit.utils.DriverUtils;

import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class FormHandleFactory {
  private final Object            driver;
  private final StatementRegistry statementRegistryForUserForms;

  public FormHandleFactory(Config config, StatementRegistry statementRegistryForUserForms) {
    this.driver = requireNonNull(config.getDriverObject());
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
        Optional.of(new FormHandle.Lambda(name)) :
        Optional.empty();
  }

  private Optional<FormHandle> createMethodBasedFormHandle(String name) {
    return this.getObjectMethodFromDriver(name).map(FormHandle.MethodBased::new);
  }

  private Optional<FormHandle> createUserDefinedFormHandle(String name) {
    return getUserDefinedStatementByName(name)
        .map(statement -> new FormHandle.User(name, statement));
  }

  private Optional<Statement> getUserDefinedStatementByName(String name) {
    return statementRegistryForUserForms.lookUp(name);
  }

  private Optional<ObjectMethod> getObjectMethodFromDriver(String methodName) {
    for (ObjectMethod each : DriverUtils.getObjectMethodsFromImportedFieldsInObject(this.driver)) {
      if (getMethodName(each).equals(methodName))
        return Optional.of(each);
    }
    return Optional.empty();
  }

  private String getMethodName(ObjectMethod method) {
    return method.getName();
  }

}
