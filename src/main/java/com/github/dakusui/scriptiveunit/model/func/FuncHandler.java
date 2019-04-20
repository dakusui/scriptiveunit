package com.github.dakusui.scriptiveunit.model.func;

import com.github.dakusui.scriptiveunit.model.Stage;

public class FuncHandler {
  public FuncHandler() {
  }

  public Object handle(FuncInvoker invoker, Form target, Stage stage, String alias) {
    return invoker.invokeFunc(target, stage, alias);
  }

  public <T> T handleConst(FuncInvoker invoker, T value) {
    return invoker.invokeConst(value);
  }
}
