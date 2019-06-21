package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

public enum BeanUtils {
  ;
  public static final Object NOP_CLAUSE = Actions.nop();

  public static <U> Form<U> toForm(Statement statement) {
    //noinspection unchecked
    return (Form) statement.compile();
  }
}
