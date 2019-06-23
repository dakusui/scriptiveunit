package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.utils.CoreUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.toArray;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public enum FormUtils {
  ;

  public static <U> Form<U> toForm(Statement statement) {
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
        ObjectMethod objectMethod = ((FormHandle.MethodBased) formHandle).objectMethod();
        return objectMethod.createFormForCompoundStatement(toArray(
            toForms(compound.getArguments()),
            Form.class
        ));
      }
      if (formHandle instanceof FormHandle.User)
        //noinspection unchecked
        return (Form<U>) createUserFunc(toArray(
            Stream.concat(
                Stream.of((Form<Statement>) input -> ((FormHandle.User) formHandle).createStatement()),
                toForms(compound.getArguments()).stream())
                .collect(toList()),
            Form.class));
      if (formHandle instanceof FormHandle.Lambda)
        //noinspection unchecked
        return (Form<U>) (Form<Form<Object>>) (Stage ii) -> getOnlyElement(toForms(compound.getArguments()));
      throw new IllegalArgumentException();
    }
    throw new IllegalArgumentException();
  }

  @SuppressWarnings("unchecked")
  private static Form<Object> createUserFunc(Form[] args) {
    return FormHandle.User.userFunc(CoreUtils.car(args), CoreUtils.cdr(args));
  }

  static <T> Form<T> createConst(T value) {
    return createProxy(
        (proxy, method, args) -> value,
        Form.Const.class);
  }

  public static List<Form> toForms(Iterable<Statement> arguments) {
    return stream(arguments.spliterator(), false)
        .map(FormUtils::toForm)
        .collect(toList());
  }

  public static <O> Form<O> createProxy(InvocationHandler handler, Class<? extends Form> interfaceClass) {
    //noinspection unchecked
    return (Form<O>) Proxy.newProxyInstance(
        Form.class.getClassLoader(),
        new Class[]{interfaceClass},
        handler
    );
  }
}
