package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormUtils;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.utils.DriverUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public interface FormHandle {

  boolean isAccessor();

  default String name() {
    throw new UnsupportedOperationException();
  }

  abstract class Base implements FormHandle {
    private final String name;

    Base(String name) {
      this.name = name;
    }

    @Override
    public String name() {
      return this.name;
    }
  }

  class Factory {
    private final Object driver;
    private final Statement.Factory statementFactory;
    private final Map<String, List<Object>> clauseMap;

    Factory(Statement.Factory statementFactory, Config config, Map<String, List<Object>> userDefinedFormClauses) {
      this.driver = requireNonNull(config.getDriverObject());
      this.statementFactory = statementFactory;
      this.clauseMap = requireNonNull(userDefinedFormClauses);
    }

    public FormHandle create(String name) {
      if ("lambda".equals(name))
        return new FormHandle.Lambda(name);
      return Factory.this.getObjectMethodFromDriver(name).map(
          (Function<ObjectMethod, FormHandle>) MethodBased::new
      ).orElseGet(
          () -> createUserForm(name)
      );
    }

    private FormHandle createUserForm(String name) {
      return new User(
          name,
          () -> statementFactory.create(
              getUserDefinedFormClauseFromSessionByName(name).orElseThrow(
                  () -> new NullPointerException(format("Undefined form '%s' was referenced.", name))
              ).get()
          )
      );
    }

    private static Form compile(Statement statement) {
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

  class MethodBased extends Base {
    final ObjectMethod objectMethod;

    private MethodBased(ObjectMethod objectMethod) {
      super(objectMethod.getName());
      this.objectMethod = objectMethod;
    }

    @Override
    public boolean isAccessor() {
      return this.objectMethod.isAccessor();
    }

    @Override
    public String toString() {
      return String.format("form:%s", this.objectMethod);
    }

    public ObjectMethod objectMethod() {
      return this.objectMethod;
    }
  }

  class Lambda extends Base {
    private Lambda(String name) {
      // TODO: need to consider how we should define a name for a lambda object
      super(name);
    }

    @Override
    public boolean isAccessor() {
      return false;
    }
  }

  class User extends Base {
    public final Supplier<Statement> userDefinedFormStatementSupplier;

    User(String name, Supplier<Statement> userDefinedFormStatementSupplier) {
      super(name);
      this.userDefinedFormStatementSupplier = userDefinedFormStatementSupplier;
    }

    @Override
    public boolean isAccessor() {
      return false;
    }

    public Statement createStatement() {
      return this.userDefinedFormStatementSupplier.get();
    }


    public static Form<Object> userFunc(Form<Statement> statementForm, Form<?>... args) {
      return (Stage input) -> Factory.compile(statementForm.apply(input)).apply(Stage.Factory.createWrappedStage(input, args));
    }
  }
}
