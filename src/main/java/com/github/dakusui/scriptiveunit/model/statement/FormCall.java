package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Form;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dakusui.scriptiveunit.core.Exceptions.SCRIPTIVEUNIT;
import static com.github.dakusui.scriptiveunit.core.Utils.check;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.indexOutOfBounds;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.toArray;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public interface FormCall {
  Form apply(FuncInvoker funcInvoker, Arguments arguments);

  boolean isAccessor();

  abstract class Base implements FormCall {
    List<Form> toFuncs(FuncInvoker funcInvoker, Iterable<Statement> arguments) {
      return stream(arguments.spliterator(), false)
          .map(statement -> statement.compile(funcInvoker))
          .collect(toList());
    }
  }

  enum Utils {
    ;

    static <T> T car(T[] arr) {
      return SCRIPTIVEUNIT.requireValue(v -> v.length > 0, SCRIPTIVEUNIT.requireNonNull(arr))[0];
    }

    static <T> T[] cdr(T[] arr) {
      return Arrays.copyOfRange(
          SCRIPTIVEUNIT.requireValue(v -> v.length > 0, SCRIPTIVEUNIT.requireNonNull(arr)),
          1,
          arr.length
      );
    }

    public static Stage createWrappedStage(Stage input, Form<?>... args) {
      return new Stage.Delegating(input) {
        @Override
        public <U> U getArgument(int index) {
          check(index < sizeOfArguments(), () -> indexOutOfBounds(index, sizeOfArguments()));
          //noinspection unchecked
          return (U) args[index].apply(input);
        }

        @Override
        public int sizeOfArguments() {
          return args.length;
        }
      };
    }
  }

  class Factory {
    private final Object                    driver;
    private final Form.Factory              funcFactory;
    private final Statement.Factory         statementFactory;
    private final Map<String, List<Object>> clauseMap;

    public Factory(Form.Factory funcFactory, Statement.Factory statementFactory, Config config, Map<String, List<Object>> userDefinedFormClauses) {
      this.driver = requireNonNull(config.getDriverObject());
      this.funcFactory = funcFactory;
      this.statementFactory = statementFactory;
      this.clauseMap = requireNonNull(userDefinedFormClauses);
    }

    @SuppressWarnings("WeakerAccess")
    public FormCall create(String name) {
      if ("lambda".equals(name))
        return new Lambda();
      return Factory.this.getObjectMethodFromDriver(name).map(
          (Function<ObjectMethod, FormCall>) MethodBasedImpl::new
      ).orElseGet(
          () -> createUserForm(name)
      );
    }

    private FormCall createUserForm(String name) {
      return new UserFormCall(
          () -> statementFactory.create(
              getUserDefinedFormClauseFromSessionByName(name).orElseThrow(
                  () -> new NullPointerException(format("Undefined form '%s' was referenced.", name))
              ).get()
          )
      );
    }

    private static Form compile(Statement statement) {
      return statement.compile(FuncInvoker.create(FuncInvoker.createMemo()));
    }

    private Optional<Supplier<List<Object>>> getUserDefinedFormClauseFromSessionByName(String name) {
      return clauseMap.containsKey(name) ?
          Optional.of(() -> clauseMap.get(name)) :
          Optional.empty();
    }

    private Optional<ObjectMethod> getObjectMethodFromDriver(String methodName) {
      for (ObjectMethod each : ScriptiveUnit.getObjectMethodsFromImportedFieldsInObject(this.driver)) {
        if (getMethodName(each).equals(methodName))
          return Optional.of(each);
      }
      return Optional.empty();
    }

    private Object[] shrinkTo(Class<?> componentType, int count, Object[] args) {
      Object[] ret = new Object[count];
      Object var = Array.newInstance(componentType, args.length - count + 1);
      if (count > 1) {
        System.arraycopy(args, 0, ret, 0, ret.length - 1);
      }
      //noinspection SuspiciousSystemArraycopy
      System.arraycopy(args, ret.length - 1, var, 0, args.length - count + 1);
      ret[ret.length - 1] = var;
      return ret;
    }

    private String getMethodName(ObjectMethod method) {
      return method.getName();
    }

    private class MethodBasedImpl extends Base {
      final ObjectMethod objectMethod;

      private MethodBasedImpl(ObjectMethod objectMethod) {
        this.objectMethod = objectMethod;
      }

      @Override
      public boolean isAccessor() {
        return this.objectMethod.isAccessor();
      }

      @Override
      public String toString() {
        return String.format("form:%s", this.objectMethod);
      }

      @Override
      public Form apply(FuncInvoker funcInvoker, Arguments arguments) {
        Form[] args = toArray(
            toFuncs(funcInvoker, arguments),
            Form.class
        );
        return createForm(funcInvoker, args);
      }

      Form createForm(FuncInvoker funcInvoker, Form[] args) {
        Object[] argValues;
        if (requireNonNull(objectMethod).isVarArgs()) {
          int parameterCount = objectMethod.getParameterCount();
          argValues = Factory.this.shrinkTo(objectMethod.getParameterTypes()[parameterCount - 1].getComponentType(), parameterCount, args);
        } else
          argValues = args;
        return funcFactory.create(funcInvoker, objectMethod, argValues);
      }
    }

    private static class UserFormCall extends Base {
      private final Supplier<Statement> userDefinedFormStatementSupplier;

      UserFormCall(Supplier<Statement> userDefinedFormStatementSupplier) {
        this.userDefinedFormStatementSupplier = userDefinedFormStatementSupplier;
      }

      @Override
      public Form<Object> apply(FuncInvoker funcInvoker, Arguments arguments) {
        return createFunc(
            toArray(
                Stream.concat(
                    Stream.of((Form<Statement>) input -> userDefinedFormStatementSupplier.get()),
                    toFuncs(funcInvoker, arguments).stream()
                ).collect(toList()),
                Form.class
            )
        );
      }

      @Override
      public boolean isAccessor() {
        return false;
      }

      @SuppressWarnings("unchecked")
      Form<Object> createFunc(Form[] args) {
        return userFunc(Utils.car(args), Utils.cdr(args));
      }

      private static Form<Object> userFunc(Form<Statement> statementForm, Form<?>... args) {
        return (Stage input) -> compile(statementForm.apply(input)).<Form<Object>>apply(Utils.createWrappedStage(input, args));
      }
    }

    private static class Lambda extends Base {

      private Lambda() {
      }

      @SuppressWarnings("unchecked")
      @Override
      public Form<Form<Object>> apply(FuncInvoker funcInvoker, Arguments arguments) {
        return (Stage ii) -> getOnlyElement(toFuncs(funcInvoker, arguments));
      }

      @Override
      public boolean isAccessor() {
        return false;
      }
    }
  }
}
