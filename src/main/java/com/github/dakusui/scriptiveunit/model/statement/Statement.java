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

import static java.util.Objects.requireNonNull;

public interface Statement {
  Func<? extends Stage, ?> execute();

  Func<? extends Stage, ?> executeWith(FuncInvoker funcInvoker);

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
      this.formFactory = new Form.Factory(testSuiteDescriptor, funcFactory);
      this.argumentsFactory = new Arguments.Factory(this);
    }

    public Statement create(Object object) {
      if (isAtom(object))
        return new Atom() {
          @Override
          public Func<Stage, Object> execute() {
            return (Func<Stage, Object>) funcFactory.createConst(object);
          }

          @Override
          public Func<Stage, Object> executeWith(FuncInvoker funcInvoker) {
            funcHandler.setFuncInvoker(funcInvoker);
            return execute();
          }
        };
      @SuppressWarnings("unchecked") List<Object> raw = (List<Object>) object;
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
          public Func<? extends Stage, ?> execute() {
            return (Func<? extends Stage, ?>) form.apply(arguments);
          }

          @Override
          public Func<? extends Stage, ?> executeWith(FuncInvoker funcInvoker) {
            funcHandler.setFuncInvoker(funcInvoker);
            return execute();
          }
        };
      } else if (car instanceof Integer) {
        return new Statement() {
          @Override
          public Func<Stage, Object> execute() {
            return new Func<Stage, Object>() {
              @Override
              public Object apply(Stage input) {
                return input.getArgument((Integer) car);
              }
            };
          }

          @Override
          public Func<Stage, Object> executeWith(FuncInvoker funcInvoker) {
            funcHandler.setFuncInvoker(funcInvoker);
            return execute();
          }
        };
      }
      throw TypeMismatch.headOfCallMustBeString(car);
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
