package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.statement.FormHandle;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import static com.google.common.collect.Iterables.toArray;

public enum BeanUtils {
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
        return ((FormHandle.MethodBasedImpl)formHandle).createForm(args);
      }
      if (formHandle instanceof FormHandle.UserFormHandle)
        return ((FormHandle.UserFormHandle)formHandle).apply(compound.getArguments());
      if (formHandle instanceof FormHandle.Lambda)
        //noinspection unchecked
        return (Form<U>) ((FormHandle.Lambda)formHandle).apply(compound.getArguments());
      throw new IllegalArgumentException();
    }
    throw new IllegalArgumentException();
  }

}
