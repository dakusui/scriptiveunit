package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.utils.CoreUtils;

import java.util.stream.Stream;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.toArray;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface FormHandle {
  <U> Form<U> toForm(Statement.Compound statement);

  default boolean isAccessor() {
    return false;
  }

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

    static <U> Form<U> methodBasedFormHandleToForm(MethodBased formHandle, Statement.Compound compound) {
      ObjectMethod objectMethod = formHandle.objectMethod();
      return objectMethod.createFormForCompoundStatement(toArray(
          FormUtils.toForms(compound.getArguments()),
          Form.class
      ));
    }

    @Override
    public <U> Form<U> toForm(Statement.Compound compound) {
      return methodBasedFormHandleToForm(this, compound);
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
      super(name);
    }

    @Override
    public <U> Form<U> toForm(Statement.Compound statement) {
      return lambdaFormHandleToForm(statement);
    }

    static <U> Form<U> lambdaFormHandleToForm(Statement.Compound compound) {
      //noinspection unchecked
      return (Form<U>) (Form<Form<Object>>) (Stage ii) -> getOnlyElement(FormUtils.toForms(compound.getArguments()));
    }

  }

  class User extends Base {
    final Statement userDefinedStatement;

    User(String name, Statement userDefinedStatement) {
      super(name);
      this.userDefinedStatement = requireNonNull(userDefinedStatement);
    }

    @Override
    public <U> Form<U> toForm(Statement.Compound statement) {
      return userFormHandleToForm(this, statement);
    }

    private static <U> Form<U> userFormHandleToForm(User formHandle, Statement.Compound compound) {
      //noinspection unchecked
      return (Form<U>) createUserFunc(toArray(
          Stream.concat(
              Stream.of((Form<Statement>) input -> formHandle.createStatement()),
              FormUtils.toForms(compound.getArguments()).stream())
              .collect(toList()),
          Form.class));
    }

    Statement createStatement() {
      return this.userDefinedStatement;
    }

    @SuppressWarnings("unchecked")
    private static Form<Object> createUserFunc(Form[] args) {
      return userFunc(CoreUtils.car(args), CoreUtils.cdr(args));
    }

    private static Form<Object> userFunc(Form<Statement> statementForm, Form<?>... args) {
      return (Stage input) -> compile(statementForm.apply(input)).apply(Stage.Factory.createWrappedStage(input, args));
    }

    private static Form compile(Statement statement) {
      return FormUtils.INSTANCE.toForm(statement);
    }

  }
}
