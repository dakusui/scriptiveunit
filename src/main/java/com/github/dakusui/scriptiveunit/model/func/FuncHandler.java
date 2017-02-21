package com.github.dakusui.scriptiveunit.model.func;

import com.github.dakusui.scriptiveunit.model.Stage;

public class FuncHandler {
  private ThreadLocal<FuncInvoker> funcInvoker = new ThreadLocal<>();

  public FuncHandler() {
  }

  public void setFuncInvoker(FuncInvoker funcInvoker) {
    this.funcInvoker.set(funcInvoker);
  }

  public Object handle(Func target, Stage stage, String alias) {
    FuncInvoker funcInvoker = this.funcInvoker.get();
    return funcInvoker.invokeFunc(target, stage, alias);
  }

  public <T> T handleConst(T value) {
    FuncInvoker funcInvoker = this.funcInvoker.get();
    return funcInvoker.invokeConst(value);
  }
}
