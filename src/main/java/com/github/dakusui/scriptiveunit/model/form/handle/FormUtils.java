package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.exceptions.SyntaxException;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.utils.CoreUtils;
import com.google.common.collect.Lists;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.toArray;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public enum FormUtils {
  INSTANCE;

  /**
   * This method returns a list of parameter names used inside a given by looking
   * into inside the statement.
   * Parameter names are names of factors defined in the test suite descriptor.
   *
   * @param statement Statement to be looked into
   * @return A list of factor names.
   */
  public static List<String> involvedParameters(Statement statement) {
    requireNonNull(statement);
    List<String> ret = Lists.newLinkedList();
    return involvedParameters(statement, ret);
  }

  public <U> Form<U> toForm(Statement statement) {
    if (statement instanceof Statement.Atom) {
      Statement.Atom atom = (Statement.Atom) statement;
      if (atom.isParameterAccessor()) {
        return input -> input.getArgument((atom.value()));
      } else {
        return createConst(atom.value());
      }
    }
    if (statement instanceof Statement.Compound) {
      Statement.Compound compound = (Statement.Compound) statement;
      FormHandle formHandle = compound.getFormHandle();
      if (formHandle instanceof FormHandle.MethodBased) {
        return methodBasedFormHandleToForm((FormHandle.MethodBased) formHandle, compound);
      }
      if (formHandle instanceof FormHandle.User) {
        return userFormHandleToForm((FormHandle.User) formHandle, compound);
      }
      if (formHandle instanceof FormHandle.Lambda) {
        return lambdaFormHandleToForm(compound);
      }
      throw new IllegalArgumentException();
    }
    throw new IllegalArgumentException();
  }

  private static <U> Form<U> lambdaFormHandleToForm(Statement.Compound compound) {
    //noinspection unchecked
    return (Form<U>) (Form<Form<Object>>) (Stage ii) -> getOnlyElement(toForms(compound.getArguments()));
  }

  private static <U> Form<U> userFormHandleToForm(FormHandle.User formHandle, Statement.Compound compound) {
    //noinspection unchecked
    return (Form<U>) createUserFunc(toArray(
        Stream.concat(
            Stream.of((Form<Statement>) input -> formHandle.createStatement()),
            toForms(compound.getArguments()).stream())
            .collect(toList()),
        Form.class));
  }

  private static <U> Form<U> methodBasedFormHandleToForm(FormHandle.MethodBased formHandle, Statement.Compound compound) {
    ObjectMethod objectMethod = formHandle.objectMethod();
    return objectMethod.createFormForCompoundStatement(toArray(
        toForms(compound.getArguments()),
        Form.class
    ));
  }

  public static List<Form> toForms(Iterable<Statement> arguments) {
    return stream(arguments.spliterator(), false)
        .map(FormUtils.INSTANCE::toForm)
        .collect(toList());
  }

  static <O> Form<O> createProxy(InvocationHandler handler, Class<? extends Form> interfaceClass) {
    //noinspection unchecked
    return (Form<O>) Proxy.newProxyInstance(
        Form.class.getClassLoader(),
        new Class[] { interfaceClass },
        handler
    );
  }

  static <T> Form<T> createConst(T value) {
    return createProxy(
        (proxy, method, args) -> value,
        Form.Const.class);
  }

  private static List<String> involvedParameters(Statement statement, List<String> work) {
    if (statement instanceof Statement.Atom)
      return work;
    if (statement instanceof Statement.Compound) {
      if (((Statement.Compound) statement).getFormHandle().isAccessor()) {
        for (Statement each : ((Statement.Compound) statement).getArguments()) {
          if (each instanceof Statement.Atom) {
            /*
             * Since this method needs to look into the internal structure of
             * the statement by evaluating it, it is valid to pass a fresh
             * memo object to an invoker.
             */
            work.add(Objects.toString(INSTANCE.toForm(each)));
          } else {
            throw SyntaxException.parameterNameShouldBeSpecifiedWithConstant((Statement.Compound) statement);
          }
        }
      } else {
        for (Statement each : ((Statement.Compound) statement).getArguments()) {
          work = involvedParameters(each, work);
        }
      }
    }
    return work;
  }

  @SuppressWarnings("unchecked")
  private static Form<Object> createUserFunc(Form[] args) {
    return userFunc(CoreUtils.car(args), CoreUtils.cdr(args));
  }

  private static Form<Object> userFunc(Form<Statement> statementForm, Form<?>... args) {
    return (Stage input) -> compile(statementForm.apply(input)).apply(Stage.Factory.createWrappedStage(input, args));
  }

  private static Form compile(Statement statement) {
    return INSTANCE.toForm(statement);
  }

}
