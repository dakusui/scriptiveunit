package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormUtils;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.utils.DriverUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class FormHandleFactory {
  private final Object driver;
  private final Statement.Factory statementFactory;
  private final Map<String, List<Object>> clauseMap;

  public FormHandleFactory(Statement.Factory statementFactory, Config config, Map<String, List<Object>> userDefinedFormClauses) {
    this.driver = requireNonNull(config.getDriverObject());
    this.statementFactory = statementFactory;
    this.clauseMap = requireNonNull(userDefinedFormClauses);
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
    return getUserDefinedFormClauseFromSessionByName(name)
        .map((Supplier<List<Object>> s) -> new FormHandle.User(name, () -> statementFactory.create(s.get())));
  }

  static Form compile(Statement statement) {
    return FormUtils.toForm(statement);
  }

  private Optional<Supplier<List<Object>>> getUserDefinedFormClauseFromSessionByName(String name) {
    return clauseMap.containsKey(name) ?
        Optional.of((Supplier<List<Object>>) () -> clauseMap.get(name)) :
        Optional.empty();
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
