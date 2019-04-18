package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.exceptions.SyntaxException;
import com.github.dakusui.scriptiveunit.exceptions.TypeMismatch;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.func.FuncHandler;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.dakusui.scriptiveunit.exceptions.TypeMismatch.headOfCallMustBeString;
import static java.util.Objects.requireNonNull;

/**
 * An interface that represents a grammatical structure of a script element.
 */
public interface Statement {
  static Factory createStatementFactory(Config config, Map<String, List<Object>> userDefinedFormClauses) {
    return new Factory(config, userDefinedFormClauses);
  }

  Func compile(FuncInvoker invoker);

  interface Atom extends Statement {
  }

  interface Nested extends Statement {
    Form getForm();

    Arguments getArguments();
  }

  class Factory {
    private final Form.Factory formFactory;
    private final Func.Factory funcFactory;

    public Factory(Config config, Map<String, List<Object>> userDefinedFormClauses) {
      FuncHandler funcHandler = new FuncHandler();
      this.funcFactory = new Func.Factory(funcHandler);
      this.formFactory = new Form.Factory(funcFactory, this, config, userDefinedFormClauses);
    }

    public Statement create(Object object) throws TypeMismatch {
      if (Utils.isAtom(object))
        return (Atom) invoker -> (Func<Object>) funcFactory.createConst(invoker, object);
      @SuppressWarnings("unchecked") List<Func> raw = (List<Func>) object;
      Object car = Utils.car(raw);
      if (car instanceof String) {
        Arguments arguments = Arguments.create(this, Utils.cdr(raw));
        Form form = this.formFactory.create((String) car);
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
        return (Atom) invoker -> (Func<Object>) input -> input.getArgument((Integer) car);
      }
      throw headOfCallMustBeString(car);
    }

  }

  enum Utils {
    ;

    /**
     * This method returns a list of parameter names used inside a given by looking
     * into inside the statement.
     * Parameter names are names of factors defined in the test suite descriptor.
     *
     * @param statement Statement to be looked into
     * @return A list of factor names.
     */
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
              /*
               * Since this method needs to look into the internal structure of
               * the statement by evaluating it, it is valid to pass a fresh
               * memo object to an invoker.
               */
              work.add(Objects.toString(each.compile(FuncInvoker.create(FuncInvoker.createMemo()))));
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
