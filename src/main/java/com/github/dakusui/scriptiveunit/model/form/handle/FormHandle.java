package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.function.Supplier;

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

  class MethodBased extends Base {
    final ObjectMethod objectMethod;

    MethodBased(ObjectMethod objectMethod) {
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

    ObjectMethod objectMethod() {
      return this.objectMethod;
    }
  }

  class Lambda extends Base {
    Lambda(String name) {
      // TODO: need to consider how we should define a name for a lambda object
      super(name);
    }

    @Override
    public boolean isAccessor() {
      return false;
    }
  }

  class User extends Base {
    final Supplier<Statement> userDefinedFormStatementSupplier;

    User(String name, Supplier<Statement> userDefinedFormStatementSupplier) {
      super(name);
      this.userDefinedFormStatementSupplier = userDefinedFormStatementSupplier;
    }

    @Override
    public boolean isAccessor() {
      return false;
    }

    Statement createStatement() {
      return this.userDefinedFormStatementSupplier.get();
    }


    static Form<Object> userFunc(Form<Statement> statementForm, Form<?>... args) {
      return (Stage input) -> FormHandleFactory.compile(statementForm.apply(input)).apply(Stage.Factory.createWrappedStage(input, args));
    }
  }
}
