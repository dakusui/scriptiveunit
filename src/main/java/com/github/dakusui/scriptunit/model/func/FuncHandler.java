package com.github.dakusui.scriptunit.model.func;

import java.lang.reflect.Method;

public class FuncHandler {
  FuncInvoker funcInvoker;

  public FuncHandler() {
  }

  public void setFuncInvoker(FuncInvoker funcInvoker) {
    this.funcInvoker = funcInvoker;
  }

  public Object invoke(Object target, Method method, Object[] args, String alias) {
    this.funcInvoker.enter();
    try {
      return this.funcInvoker.invokeMethod(target, method, args, alias);
    } finally {
      this.funcInvoker.leave();
    }
  }

  public <T> T handleConst(T value) {
    this.funcInvoker.enter();
    try {
      return this.funcInvoker.invokeConst(value);
    } finally {
      this.funcInvoker.leave();
    }
  }
}
