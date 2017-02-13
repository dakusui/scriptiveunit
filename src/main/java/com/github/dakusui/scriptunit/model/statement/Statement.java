package com.github.dakusui.scriptunit.model.statement;

import com.github.dakusui.scriptunit.exceptions.SyntaxException;
import com.github.dakusui.scriptunit.model.Func;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public interface Statement {
  Object execute();

  interface Atom extends Statement {
  }

  interface Nested extends Statement {
    Form getForm();

    Arguments getArguments();
  }

  class Factory {
    private final Form.Factory      formFactory;
    private final Arguments.Factory argumentsFactory;

    public Factory(Form.Factory formFactory) {
      this.formFactory = formFactory;
      this.argumentsFactory = new Arguments.Factory(this);
    }

    public Statement create(Object object, Func.Invoker funcInvoker) {
      if (isAtom(object))
        return (Atom) () -> funcInvoker.createConst(object);
      @SuppressWarnings("unchecked") List<Object> raw = (List<Object>) object;
      Form form = this.formFactory.create(String.class.cast(car(raw)), funcInvoker);
      Arguments arguments = this.argumentsFactory.create(cdr(raw), funcInvoker);
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
              work.add(Objects.toString(each.execute()));
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
