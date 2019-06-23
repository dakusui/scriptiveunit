package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.exceptions.SyntaxException;
import com.github.dakusui.scriptiveunit.exceptions.TypeMismatch;
import com.github.dakusui.scriptiveunit.model.form.handle.FormHandle;
import com.github.dakusui.scriptiveunit.model.form.handle.FormHandleFactory;
import com.github.dakusui.scriptiveunit.model.form.handle.FormRegistry;
import com.github.dakusui.scriptiveunit.model.form.handle.FormUtils;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.utils.CoreUtils;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.dakusui.scriptiveunit.exceptions.TypeMismatch.headOfCallMustBeString;
import static java.util.Objects.requireNonNull;

/**
 * An interface that represents a lexical structure of a script element.
 */
public interface Statement {
  static Factory createStatementFactory(Config config, Map<String, List<Object>> userDefinedFormClauses) {
    return new Factory(config, userDefinedFormClauses);
  }

  <V> V evaluate(Stage stage);

  interface Atom extends Statement {
    default boolean isParameterAccessor() {
      return false;
    }

    default <V> V value() {
      throw new UnsupportedOperationException();
    }
  }

  interface Compound extends Statement {
    FormHandle getFormHandle();

    Arguments getArguments();

    @SuppressWarnings("unchecked")
    default <V> V evaluate(Stage stage) {
      return (V) stage.formRegistry()
          .lookUp(this.getFormHandle())
          .orElseThrow(FormRegistry.Utils.undefinedFormError(this))
          .apply(stage.createChild(this));
    }
  }

  class Factory {
    private final FormHandleFactory formHandleFactory;

    public Factory(Config config, Map<String, List<Object>> userDefinedFormClauses) {
      this.formHandleFactory = new FormHandleFactory(
          this,
          config,
          userDefinedFormClauses);
    }

    public Statement create(Object object) throws TypeMismatch {
      if (CoreUtils.isAtom(object))
        return createAtom(object);
      @SuppressWarnings("unchecked") List<Object> raw = (List<Object>) object;
      Object car = CoreUtils.car(raw);
      if (car instanceof String) {
        Arguments arguments = Arguments.create(this, CoreUtils.cdr(raw));
        FormHandle formHandle = this.formHandleFactory.create((String) car);
        return new Compound() {
          @Override
          public FormHandle getFormHandle() {
            return formHandle;
          }

          @Override
          public Arguments getArguments() {
            return arguments;
          }
        };
      } else if (car instanceof Integer) {
        return new Atom() {
          @Override
          public <V> V evaluate(Stage stage) {
            return stage.getArgument((Integer) car);
          }

          @SuppressWarnings("unchecked")
          public <V> V value() {
            return (V) car;
          }

          @Override
          public boolean isParameterAccessor() {
            return true;
          }
        };
      }
      throw headOfCallMustBeString(car);
    }

    Atom createAtom(Object object) {
      return new Atom() {
        @SuppressWarnings("unchecked")
        @Override
        public <V> V evaluate(Stage stage) {
          return (V) object;
        }

        @SuppressWarnings("unchecked")
        public <V> V value() {
          if (isParameterAccessor())
            throw new IllegalStateException();
          return (V) object;
        }
      };
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
      if (statement instanceof Compound) {
        if (((Compound) statement).getFormHandle().isAccessor()) {
          for (Statement each : ((Compound) statement).getArguments()) {
            if (each instanceof Atom) {
              /*
               * Since this method needs to look into the internal structure of
               * the statement by evaluating it, it is valid to pass a fresh
               * memo object to an invoker.
               */
              work.add(Objects.toString(FormUtils.toForm(each)));
            } else {
              throw SyntaxException.parameterNameShouldBeSpecifiedWithConstant((Compound) statement);
            }
          }
        } else {
          for (Statement each : ((Compound) statement).getArguments()) {
            work = involvedParameters(each, work);
          }
        }
      }
      return work;
    }
  }
}
