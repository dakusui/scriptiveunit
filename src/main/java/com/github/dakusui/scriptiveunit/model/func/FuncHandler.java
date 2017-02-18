package com.github.dakusui.scriptiveunit.model.func;

import java.lang.reflect.Method;

public class FuncHandler {
  private ThreadLocal<FuncInvoker> funcInvoker = new ThreadLocal<>();

  public FuncHandler() {
  }

  public void setFuncInvoker(FuncInvoker funcInvoker) {
    this.funcInvoker.set(funcInvoker);
  }

  public Object invoke(Object target, Method method, Object[] args, String alias) {
    FuncInvoker funcInvoker = this.funcInvoker.get();
    return funcInvoker.invokeMethod(target, method, args, alias);
  }

  public <T> T handleConst(T value) {
    FuncInvoker funcInvoker = this.funcInvoker.get();
    return funcInvoker.invokeConst(value);
  }
}
