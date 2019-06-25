package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.exceptions.TypeMismatch;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.handle.FormHandle;
import com.github.dakusui.scriptiveunit.model.form.handle.FormHandleFactory;
import com.github.dakusui.scriptiveunit.model.form.handle.ObjectMethodRegistry;
import com.github.dakusui.scriptiveunit.utils.CoreUtils;

import java.util.List;
import java.util.Map;

import static com.github.dakusui.scriptiveunit.exceptions.TypeMismatch.headOfCallMustBeString;
import static com.github.dakusui.scriptiveunit.model.form.handle.FormUtils.createConst;

/**
 * An interface that represents a lexical structure of a script element.
 */
public interface Statement {
  static Factory createStatementFactory(Config config, Map<String, List<Object>> userDefinedFormClauses) {
    return new Factory(ObjectMethodRegistry.load(config.getDriverObject()), userDefinedFormClauses);
  }

  <U> Form<U> toForm();

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
  }

  class Factory {
    private final FormHandleFactory formHandleFactory;

    public Factory(ObjectMethodRegistry objectMethodRegistry, Map<String, List<Object>> userDefinedFormClauses) {

      this.formHandleFactory = new FormHandleFactory(
          objectMethodRegistry,
          StatementRegistry.create(this, userDefinedFormClauses));
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
          public <U> Form<U> toForm() {
            return getFormHandle().toForm(this);
          }

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
          @SuppressWarnings("unchecked")
          public <V> V value() {
            return (V) car;
          }

          @Override
          public <U> Form<U> toForm() {
            return input -> input.getArgument((this.value()));
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
        @Override
        public <U> Form<U> toForm() {
          return createConst(this.value());
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

}
