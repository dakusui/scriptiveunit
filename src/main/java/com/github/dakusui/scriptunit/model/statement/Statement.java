package com.github.dakusui.scriptunit.model.statement;

import com.github.dakusui.scriptunit.exceptions.SyntaxException;
import com.github.dakusui.scriptunit.model.func.Func;
import com.github.dakusui.scriptunit.model.func.FuncHandler;
import com.github.dakusui.scriptunit.model.func.FuncInvoker;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public interface Statement {
  Object execute();

  Object executeWith(FuncInvoker funcInvoker);

  interface Atom extends Statement {

  }

  interface Nested extends Statement {
    Form getForm();

    Arguments getArguments();
  }

  class Factory {
    private final Form.Factory      formFactory;
    private final Arguments.Factory argumentsFactory;
    private final Func.Factory      funcFactory;
    private final FuncHandler       funcHandler;
    private final Object            driverObject;

    public Factory(Object driverObject) {
      this.driverObject = driverObject;
      this.funcHandler = new FuncHandler();
      this.funcFactory = new Func.Factory(funcHandler);
      this.formFactory = new Form.Factory(driverObject, funcFactory);
      this.argumentsFactory = new Arguments.Factory(this);
    }

    public Statement create(Object object) {
      if (isAtom(object))
        return new Atom() {
          @Override
          public Object execute() {
            return funcFactory.createConst(object);
          }

          @Override
          public Object executeWith(FuncInvoker funcInvoker) {
            funcHandler.setFuncInvoker(funcInvoker);
            return execute();
          }
        };
      @SuppressWarnings("unchecked") List<Object> raw = (List<Object>) object;
      Form form = this.formFactory.create(String.class.cast(car(raw)), funcHandler);
      Arguments arguments = this.argumentsFactory.create(cdr(raw), funcHandler);
      return new Nested() {
        @Override
        public Form getForm() {
          return form;
        }

        @Override
        public Arguments getArguments() {
          return arguments;
        }

        @Override
        public Object execute() {
          return form.apply(arguments);
        }

        @Override
        public Object executeWith(FuncInvoker funcInvoker) {
          funcHandler.setFuncInvoker(funcInvoker);
          return execute();
        }
      };
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

  enum Utils {
    ;

    public static List<String> involvedParameters(Statement statement) {
      requireNonNull(statement);
      List<String> ret = Lists.newLinkedList();
      return involvedParameters(statement, ret);
    }

    private static List<String> involvedParameters(Statement statement, List<String> work) {
      if (statement instanceof Atom)
        return work;
      if (statement instanceof Nested) {
        if (((Nested) statement).getForm().isAccessor()) {
          for (Statement each : ((Nested) statement).getArguments()) {
            if (each instanceof Atom) {
              work.add(Objects.toString(each.executeWith(new FuncInvoker.Impl(0))));
            } else {
              throw SyntaxException.parameterNameShouldBeSpecifiedWithConstant((Nested) statement);
            }
          }
        } else {
          for (Statement each : ((Nested) statement).getArguments()) {
            work = involvedParameters(each, work);
          }
        }
      }
      return work;
    }
  }
}
