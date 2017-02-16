package com.github.dakusui.scriptunit.model.func;

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
    funcInvoker.enter();
    try {
      return funcInvoker.invokeMethod(target, method, args, alias);
    } finally {
      funcInvoker.leave();
    }
  }

  public <T> T handleConst(T value) {
    FuncInvoker funcInvoker = this.funcInvoker.get();
    funcInvoker.enter();
    try {
      return funcInvoker.invokeConst(value);
    } finally {
      funcInvoker.leave();
    }
  }
}
