package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.statement.FormHandle;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

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
      if (formHandle instanceof FormHandle.Factory.MethodBasedImpl)
        return formHandle.apply(compound.getArguments());
      if (formHandle instanceof FormHandle.Factory.UserFormHandle)
        return formHandle.apply(compound.getArguments());
      if (formHandle instanceof FormHandle.Factory.Lambda)
        return formHandle.apply(compound.getArguments());
      throw new IllegalArgumentException();
    }
    throw new IllegalArgumentException();
  }

}
