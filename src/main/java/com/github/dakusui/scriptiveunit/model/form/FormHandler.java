package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.session.Stage;

public class FormHandler {
  public FormHandler() {
  }

  Object handle(FormInvoker invoker, Form target, Stage stage, String alias) {
    return invoker.invokeFunc(target, stage, alias);
  }

  <T> T handleConst(FormInvoker invoker, T value) {
    return invoker.invokeConst(value);
  }
}
