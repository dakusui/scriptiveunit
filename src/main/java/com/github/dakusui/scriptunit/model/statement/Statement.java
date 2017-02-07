package com.github.dakusui.scriptunit.model.statement;

import com.github.dakusui.scriptunit.model.Func;

import java.util.List;

public interface Statement {
  Object execute();

  class Factory {
    private final Form.Factory      formFactory;
    private final Arguments.Factory argumentsFactory;

    public Factory(Form.Factory formFactory) {
      this.formFactory = formFactory;
      this.argumentsFactory = new Arguments.Factory(this);
    }

    public Statement create(Object object, Func.Invoker funcInvoker) {
      if (isAtom(object))
        return () -> funcInvoker.createConst(object);
      @SuppressWarnings("unchecked") List<Object> raw = (List<Object>) object;
      Form form = this.formFactory.create(String.class.cast(car(raw)), funcInvoker);
      Arguments arguments = this.argumentsFactory.create(cdr(raw), funcInvoker);
      return () -> form.apply(arguments);
    }

    static boolean isAtom(Object object) {
      return !(object instanceof List) || ((List) object).isEmpty();
    }

    static Object car(List<Object> raw) {
      return raw.get(0);
    }

    static List<Object> cdr(List<Object> raw) {
      return raw.subList(1, raw.size());
    }
  }
}
