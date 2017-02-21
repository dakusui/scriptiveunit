package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.exceptions.SyntaxException;
import com.github.dakusui.scriptiveunit.exceptions.TypeMismatch;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.func.FuncHandler;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

import static com.github.dakusui.scriptiveunit.exceptions.TypeMismatch.headOfCallMustBeString;
import static java.util.Objects.requireNonNull;

public interface Statement {
  Func execute(FuncInvoker invoker);

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

    public Factory(TestSuiteDescriptor testSuiteDescriptor) {
      this.funcHandler = new FuncHandler();
      this.funcFactory = new Func.Factory(funcHandler);
      this.argumentsFactory = new Arguments.Factory(this);
      this.formFactory = new Form.Factory(testSuiteDescriptor, funcFactory, this);
    }

    public Statement create(Object object) throws TypeMismatch {
      if (isAtom(object))
        return (Atom) invoker -> (Func<Object>) funcFactory.createConst(invoker, object);
      @SuppressWarnings("unchecked") List<Func> raw = (List<Func>) object;
      Object car = car(raw);
      if (car instanceof String) {
        Form form = this.formFactory.create(String.class.cast(car));
        Arguments arguments = this.argumentsFactory.create(cdr(raw));
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
          public Func<?> execute(FuncInvoker invoker) {
            return (Func<?>) form.apply(invoker, arguments);
          }
        };
      } else if (car instanceof Integer) {
        return invoker -> (Func<Object>) input -> input.getArgument((Integer) car);
      }
      throw headOfCallMustBeString(car);
    }

    static boolean isAtom(Object object) {
      return !(object instanceof List) || ((List) object).isEmpty();
    }

    static Object car(List<Func> raw) {
      return raw.get(0);
    }

    static List<Func> cdr(List<Func> raw) {
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
              work.add(Objects.toString(each.execute(new FuncInvoker.Impl(0))));
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
