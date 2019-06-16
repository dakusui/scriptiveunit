package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.scriptiveunit.model.func.Form;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

public enum BeanUtils {
  ;
  public static final Object NOP_CLAUSE = Actions.nop();

  public static <U> Form<U> toFunc(Statement statement, FuncInvoker funcInvoker) {
    //noinspection unchecked
    return (Form) statement.compile(funcInvoker);
  }
}
