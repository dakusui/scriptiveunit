package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.exceptions.SyntaxException;
import com.github.dakusui.scriptiveunit.exceptions.TypeMismatch;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.func.FuncHandler;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

import static com.github.dakusui.scriptiveunit.exceptions.TypeMismatch.headOfCallMustBeString;
import static java.util.Objects.requireNonNull;

/**
 * An interface that represents a grammatical structure of a script element.
 */
public interface Statement {
  Func compile(FuncInvoker invoker);

  String format();

  interface Atom extends Statement {
    default String format() {
      return "atom";
    }
  }

  interface Nested extends Statement {
    Form getForm();

    Arguments getArguments();

    default String format() {
      return String.format("nested:%s,%s", getForm(), getArguments());
    }
  }

  class Factory {
    private final Form.Factory formFactory;
    private final Func.Factory funcFactory;
    private final FuncHandler  funcHandler;

    public Factory(Session session) {
      this.funcHandler = new FuncHandler();
      this.funcFactory = new Func.Factory(funcHandler);
      this.formFactory = new Form.Factory(session, funcFactory, this);
    }

    public Statement create(Object object) throws TypeMismatch {
      if (Utils.isAtom(object))
        return (Atom) invoker -> (Func<Object>) funcFactory.createConst(invoker, object);
      @SuppressWarnings("unchecked") List<Func> raw = (List<Func>) object;
      Object car = Utils.car(raw);
      if (car instanceof String) {
        Form form = this.formFactory.create(String.class.cast(car));
        Arguments arguments = Arguments.create(this, Utils.cdr(raw));
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
          public Func<?> compile(FuncInvoker invoker) {
            return (Func<?>) getForm().apply(invoker, arguments);
          }
        };
      } else if (car instanceof Integer) {
        return (Atom) invoker -> (Func<Object>) new Func<Object>() {
          @Override
          public Object apply(Stage input) {
            return input.getArgument((Integer) car);
          }
        };
      }
      throw headOfCallMustBeString(car);
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
              work.add(Objects.toString(each.compile(new FuncInvoker.Impl(0, FuncInvoker.createMemo()))));
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
}
