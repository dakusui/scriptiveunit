package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.core.Exceptions;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dakusui.scriptiveunit.core.Utils.check;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.indexOutOfBounds;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.toArray;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public interface Form {
  Func apply(FuncInvoker funcInvoker, Arguments arguments);

  boolean isAccessor();

  abstract class Base implements Form {
    List<Func> toFuncs(FuncInvoker funcInvoker, Arguments arguments) {
      return stream(
          arguments.spliterator(), false
      ).map(
          statement -> statement.compile(funcInvoker)
      ).collect(
          toList()
      );
    }
  }

  enum Utils {
    ;

    static <T> T car(T[] arr) {
      return Exceptions.I.requireValue(v -> v.length > 0, Exceptions.I.requireNonNull(arr))[0];
    }

    static <T> T[] cdr(T[] arr) {
      return Arrays.copyOfRange(
          Exceptions.I.requireValue(v -> v.length > 0, Exceptions.I.requireNonNull(arr)),
          1,
          arr.length
      );
    }

    public static Stage createWrappedStage(Stage input, Func<?>... args) {
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
    private final Object            driver;
    private final Func.Factory      funcFactory;
    private final Statement.Factory statementFactory;
    private final Session           session;

    public Factory(Session session, Func.Factory funcFactory, Statement.Factory statementFactory) {
      this.driver = requireNonNull(session.getConfig().getDriverObject());
      this.funcFactory = funcFactory;
      this.statementFactory = statementFactory;
      this.session = session;
    }

    @SuppressWarnings("WeakerAccess")
    public Form create(String name) {
      if ("lambda".equals(name))
        return new Lambda();
      return Factory.this.getObjectMethodFromDriver(name).map(
          (Function<ObjectMethod, Form>) MethodBasedImpl::new
      ).orElseGet(
          () -> createUserForm(name)
      );
    }

    private Form createUserForm(String name) {
      return new UserForm(
          () -> statementFactory.create(
              getUserDefinedFormClauseFromSessionByName(name).orElseThrow(
                  () -> new NullPointerException(format("Undefined form '%s' was referenced.", name))
              ).get()
          )
      );
    }

    private static Func toFunc(Statement statement) {
      return statement.compile(FuncInvoker.create());
    }

    private Optional<Supplier<List<Object>>> getUserDefinedFormClauseFromSessionByName(String name) {
      Map<String, List<Object>> clauseMap = session.loadTestSuiteDescriptor().getUserDefinedFormClauses();
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
      public Func apply(FuncInvoker funcInvoker, Arguments arguments) {
        Func[] args = toArray(
            toFuncs(funcInvoker, arguments),
            Func.class
        );
        return createFunc(funcInvoker, args);
      }

      Func createFunc(FuncInvoker funcInvoker, Func[] args) {
        Object[] argValues;
        if (requireNonNull(objectMethod).isVarArgs()) {
          int parameterCount = objectMethod.getParameterCount();
          argValues = Factory.this.shrinkTo(objectMethod.getParameterTypes()[parameterCount - 1].getComponentType(), parameterCount, args);
        } else
          argValues = args;
        return funcFactory.create(funcInvoker, objectMethod, argValues);
      }
    }

    private static class UserForm extends Base {
      private final Supplier<Statement> userDefinedFormStatementSupplier;

      UserForm(Supplier<Statement> userDefinedFormStatementSupplier) {
        this.userDefinedFormStatementSupplier = userDefinedFormStatementSupplier;
      }

      @Override
      public Func apply(FuncInvoker funcInvoker, Arguments arguments) {
        return createFunc(
            toArray(
                Stream.concat(
                    Stream.of((Func<Statement>) input -> userDefinedFormStatementSupplier.get()),
                    toFuncs(funcInvoker, arguments).stream()
                ).collect(toList()),
                Func.class
            )
        );
      }

      @Override
      public boolean isAccessor() {
        return false;
      }

      @SuppressWarnings("unchecked")
      Func createFunc(Func[] args) {
        return userFunc(Utils.car(args), Utils.cdr(args));
      }

      private static Func<Object> userFunc(Func<Statement> funcBody, Func<?>... args) {
        return (Stage input) -> toFunc(funcBody.apply(input)).<Func<Object>>apply(Utils.createWrappedStage(input, args));
      }
    }

    private static class Lambda extends Base {

      private Lambda() {
      }

      @Override
      public Func apply(FuncInvoker funcInvoker, Arguments arguments) {
        return (Func<Object>) input -> (Func<Object>) i -> getOnlyElement(toFuncs(funcInvoker, arguments)).apply(input);
      }

      @Override
      public boolean isAccessor() {
        return false;
      }
    }
  }
}
