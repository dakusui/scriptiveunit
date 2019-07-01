package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.model.form.Form;
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

public interface FormHandle {
  <U> Form<U> toForm(Statement.Compound statement);

  boolean isAccessor();

  interface Default extends FormHandle {
    @Override
    default boolean isAccessor() {
      return false;
    }
  }

  abstract class Base implements FormHandle.Default {
    @Override
    public abstract String toString();
  }

  class MethodBased extends Base {
    final ObjectMethod objectMethod;

    MethodBased(ObjectMethod objectMethod) {
      this.objectMethod = objectMethod;
    }

    static <U> Form<U> methodBasedFormHandleToForm(MethodBased formHandle, Statement.Compound compound) {
      ObjectMethod objectMethod = formHandle.objectMethod();
      return objectMethod.createForm(
          iterableToStream(compound.getArguments())
              .map(Statement::toForm)
              .toArray(Form[]::new));
    }

    @Override
    public <U> Form<U> toForm(Statement.Compound compound) {
      return Form.Named.create(
          this.objectMethod.getName(),
          methodBasedFormHandleToForm(this, compound));
    }

    @Override
    public boolean isAccessor() {
      return this.objectMethod.isAccessor();
    }

    @Override
    public String toString() {
      return this.objectMethod.getName();
    }

    ObjectMethod objectMethod() {
      return this.objectMethod;
    }
  }

  class Lambda extends Base {
    Lambda() {
    }

    @Override
    public <U> Form<U> toForm(Statement.Compound statement) {
      return Form.Named.create(
          "<lambda>",
          lambdaFormHandleToForm(statement)
      );
    }

    static <U> Form<U> lambdaFormHandleToForm(Statement.Compound compound) {
      //noinspection unchecked
      return (Form<U>) (Form<Form<Object>>) (Stage ii) ->
          getOnlyElement(iterableToStream(compound.getArguments())
              .map(Statement::toForm)
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
    public <U> Form<U> toForm(Statement.Compound statement) {
      return userFormHandleToForm(this, statement);
    }

    private static <U> Form<U> userFormHandleToForm(User formHandle, Statement.Compound compound) {
      //noinspection unchecked
      return (Form<U>) createUserFunc(toArray(
          Stream.concat(
              Stream.of((Form<Statement>) input -> formHandle.statement()),
              iterableToStream(compound.getArguments()).map(Statement::toForm))
              .collect(toList()),
          Form.class));
    }

    private Statement statement() {
      return this.userDefinedStatement;
    }

    @SuppressWarnings("unchecked")
    private static Form<Object> createUserFunc(Form[] args) {
      return Form.Named.create("<user>", userFunc(CoreUtils.car(args), CoreUtils.cdr(args)));
    }

    private static Form<Object> userFunc(Form<Statement> statementForm, Form<?>... args) {
      return input -> statementForm
          .apply(input)
          .toForm()
          .apply(createWrappedStage(input, args));
    }

    @Override
    public String toString() {
      return "<user>:" + userDefinedStatement;
    }
  }
}
