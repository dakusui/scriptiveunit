package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.FormHandle;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.github.dakusui.scriptiveunit.core.Exceptions.SCRIPTIVEUNIT;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.toArray;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public enum FormUtils {
  ;
  private static final Form.Factory FORM_FACTORY = new Form.Factory();

  public static <U> Form<U> toForm(Statement statement) {
    if (statement instanceof Statement.Atom) {
      Statement.Atom atom = (Statement.Atom) statement;
      if (atom.isParameterAccessor()) {
        return input -> input.getArgument((atom.value()));
      } else {
        return FORM_FACTORY.createConst(atom.value());
      }
    }
    if (statement instanceof Statement.Compound) {
      Statement.Compound compound = (Statement.Compound) statement;
      FormHandle formHandle = compound.getFormHandle();
      if (formHandle instanceof FormHandle.MethodBasedImpl) {
        Form[] args = toArray(
            FormHandle.toForms(compound.getArguments()),
            Form.class
        );
        // TODO a form doesn't need to know a FormInvoker with which it will be invoked.
        Object[] argValues;
        if (requireNonNull(((FormHandle.MethodBasedImpl) formHandle).objectMethod).isVarArgs()) {
          int parameterCount = ((FormHandle.MethodBasedImpl) formHandle).objectMethod.getParameterCount();
          argValues = ((FormHandle.MethodBasedImpl) formHandle).formHandleFactory.shrinkTo(((FormHandle.MethodBasedImpl) formHandle).objectMethod.getParameterTypes()[parameterCount - 1].getComponentType(), parameterCount, args);
        } else
          argValues = args;
        return ((FormHandle.MethodBasedImpl) formHandle).formFactory.create(((FormHandle.MethodBasedImpl) formHandle).objectMethod, argValues);
      }
      if (formHandle instanceof FormHandle.UserFormHandle)
        //noinspection unchecked
        return (Form<U>) createFunc(
            toArray(
                Stream.concat(
                    Stream.of((Form<Statement>) input -> ((FormHandle.UserFormHandle) formHandle).userDefinedFormStatementSupplier.get()),
                    FormHandle.toForms(compound.getArguments()).stream()
                ).collect(toList()),
                Form.class
            )
        );
      if (formHandle instanceof FormHandle.Lambda)
        //noinspection unchecked
        return (Form<U>) (Form<Form<Object>>) (Stage ii) -> getOnlyElement(FormHandle.toForms(compound.getArguments()));
      throw new IllegalArgumentException();
    }
    throw new IllegalArgumentException();
  }

  @SuppressWarnings("unchecked")
  public static Form<Object> createFunc(Form[] args) {
    return FormHandle.UserFormHandle.userFunc(car(args), cdr(args));
  }

  static <T> T car(T[] arr) {
    return SCRIPTIVEUNIT.requireValue(v -> v.length > 0, SCRIPTIVEUNIT.requireNonNull(arr))[0];
  }

  static <T> T[] cdr(T[] arr) {
    return Arrays.copyOfRange(
        SCRIPTIVEUNIT.requireValue(v -> v.length > 0, SCRIPTIVEUNIT.requireNonNull(arr)),
        1,
        arr.length
    );
  }
}
