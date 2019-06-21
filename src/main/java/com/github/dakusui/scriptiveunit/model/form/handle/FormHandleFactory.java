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
import java.util.function.Function;
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
    if ("lambda".equals(name))
      return new FormHandle.Lambda(name);
    return FormHandleFactory.this.getObjectMethodFromDriver(name).map(
        (Function<ObjectMethod, FormHandle>) FormHandle.MethodBased::new
    ).orElseGet(
        () -> createUserForm(name)
    );
  }

  private FormHandle createUserForm(String name) {
    return new FormHandle.User(
        name,
        () -> statementFactory.create(
            getUserDefinedFormClauseFromSessionByName(name).orElseThrow(
                () -> new NullPointerException(format("Undefined form '%s' was referenced.", name))
            ).get()
        )
    );
  }

  static Form compile(Statement statement) {
    return FormUtils.toForm(statement);
  }

  private Optional<Supplier<List<Object>>> getUserDefinedFormClauseFromSessionByName(String name) {
    return clauseMap.containsKey(name) ?
        Optional.of(() -> clauseMap.get(name)) :
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
