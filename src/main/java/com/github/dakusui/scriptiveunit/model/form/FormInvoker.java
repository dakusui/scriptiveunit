package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.loaders.beans.FormInvokerImpl;

public interface FormInvoker {
  static FormInvoker create() {
    return new FormInvokerImpl();
  }

  String asString();
}
