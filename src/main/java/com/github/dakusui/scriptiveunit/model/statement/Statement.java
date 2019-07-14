package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.exceptions.TypeMismatch;
import com.github.dakusui.scriptiveunit.model.form.Value;
import com.github.dakusui.scriptiveunit.model.form.handle.ValueResolverHandle;
import com.github.dakusui.scriptiveunit.model.form.handle.ValueResolverHandleFactory;
import com.github.dakusui.scriptiveunit.model.form.handle.ValueResolverRegistry;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.utils.CoreUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static com.github.dakusui.scriptiveunit.exceptions.TypeMismatch.headOfCallMustBeString;

/**
 * An interface that represents a lexical structure of a script element.
 */
public interface Statement {
  static Factory createStatementFactory(Config config, Map<String, List<Object>> userDefinedFormClauseMap) {
    return new Factory(ValueResolverRegistry.load(config.getDriverObject()), userDefinedFormClauseMap);
  }

  <U> Value<U> toValue();

  default void accept(Visitor visitor) {
    throw new UnsupportedOperationException();
  }

  interface Atom extends Statement {
    default boolean isParameterAccessor() {
      return false;
    }

    default void accept(Visitor visitor) {
      visitor.visit(this);
    }

    default <V> V value() {
      throw new UnsupportedOperationException();
    }
  }

  interface Compound extends Statement {
    ValueResolverHandle getValueResolverHandle();

    Arguments getArguments();

    default void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }

  class Factory {
    private final ValueResolverHandleFactory valueResolverHandleFactory;

    public Factory(ValueResolverRegistry valueResolverRegistry, Map<String, List<Object>> userDefinedFormClauses) {

      this.valueResolverHandleFactory = new ValueResolverHandleFactory(
          valueResolverRegistry,
          StatementRegistry.create(this, userDefinedFormClauses));
    }

    public Statement create(Object object) throws TypeMismatch {
      if (CoreUtils.isAtom(object))
        return createAtom(object);
      @SuppressWarnings("unchecked") List<Object> raw = (List<Object>) object;
      Object car = CoreUtils.car(raw);
      if (car instanceof String) {
        Arguments arguments = Arguments.create(this, CoreUtils.cdr(raw));
        ValueResolverHandle valueResolverHandle = this.valueResolverHandleFactory.create((String) car);
        return new Compound() {
          @Override
          public <U> Value<U> toValue() {
            return getValueResolverHandle().toValue(this);
          }

          @Override
          public ValueResolverHandle getValueResolverHandle() {
            return valueResolverHandle;
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
          public <U> Value<U> toValue() {
            Value<U> value = input -> input.getArgument((this.value()));
            return new Value<U>() {
              @Override
              public U apply(Stage stage) {
                return Stage.evaluateValue(stage, value, Value::apply);
              }

              @Override
              public String name() {
                return String.format("arg[%s]", car);
              }

              @Override
              public String toString() {
                return value.toString();
              }
            };
          }

          @Override
          public boolean isParameterAccessor() {
            return true;
          }

          @Override
          public String toString() {
            return String.format("(%s)", car);
          }
        };
      }
      throw headOfCallMustBeString(car);
    }

    Atom createAtom(Object object) {
      return new Atom() {
        @Override
        public <U> Value<U> toValue() {
          return new Value.Const<U>() {
            @Override
            public U apply(Stage stage) {
              return Stage.evaluateValue(stage, this, (f, s) -> value());
            }

            @Override
            public String name() {
              return "const:'" + value() + "'";
            }

            @Override
            public String toString() {
              return Objects.toString(value());//form.toString();
            }
          };
        }

        @SuppressWarnings("unchecked")
        public <V> V value() {
          if (isParameterAccessor())
            throw new IllegalStateException();
          return (V) object;
        }

        @Override
        public String toString() {
          return String.format("\"%s\"", Objects.toString(this.value()));
        }
      };
    }
  }

  interface Visitor {
    void visit(Statement.Atom atom);

    void visit(Statement.Compound compound);

    class Formatter implements Visitor {
      StringBuilder b = new StringBuilder();

      @Override
      public void visit(Atom atom) {
        b.append(atom);
      }

      @Override
      public void visit(Compound compound) {
        b.append("(");
        b.append(compound.getValueResolverHandle().toString());
        //
        StreamSupport.stream(compound.getArguments().spliterator(), false)
            .peek(each -> b.append(" "))
            .forEach(each -> each.accept(this));
        b.append(")");
      }

      public String toString() {
        return b.toString();
      }
    }
  }

  static String format(Statement statement) {
    Visitor.Formatter formatter = new Visitor.Formatter();
    statement.accept(formatter);
    return formatter.toString();
  }
}
