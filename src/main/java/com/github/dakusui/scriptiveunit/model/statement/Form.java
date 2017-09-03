package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Exceptions;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.dakusui.scriptiveunit.core.Utils.check;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.indexOutOfBounds;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.toArray;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public interface Form {
  Func apply(FuncInvoker funcInvoker, Arguments arguments);

  boolean isAccessor();

  abstract class Base implements Form {
    final ObjectMethod objectMethod;

    Base(ObjectMethod objectMethod) {
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
          (Function<ObjectMethod, Form>) FormImpl::new
      ).orElseGet(
          () -> createUserForm(name)
      );
    }

    private Form createUserForm(String name) {
      return new UserForm(
          rename(
              userFunc(),
              name
          ),
          getUserDefinedFormClauseFromSessionByName(name).orElseThrow(
              () -> new NullPointerException(format("Undefined form '%s' was referenced.", name))
          )
      );
    }

    /*
     * This method is referenced reflectively.
     */
    @SuppressWarnings({ "unused", "WeakerAccess" })
    @Scriptable
    public static Func<Object> userFunc(Func<List<Object>> funcBody, Func<?>... args) {

      return (Stage input) -> {
        Stage wrappedStage = createWrappedStage(input, args);
        return toFunc(funcBody, wrappedStage)
            .<Func<Object>>apply(wrappedStage);
      };
    }

    public static Func toFunc(Func<List<Object>> funcBody, Stage wrappedStage) {
      return toStatement(funcBody, wrappedStage)
          .compile(new FuncInvoker.Impl(0, FuncInvoker.createMemo()));
    }

    private static Statement toStatement(Func<List<Object>> funcBody, Stage wrappedStage) {
      return wrappedStage.getStatementFactory().create(funcBody.apply(wrappedStage));
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

    private ObjectMethod userFunc() {
      try {
        return ObjectMethod.create(null, Form.Factory.class.getMethod("userFunc", Func.class, Func[].class), Collections.emptyMap());
      } catch (NoSuchMethodException e) {
        throw ScriptiveUnitException.wrap(e);
      }
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

    private ObjectMethod rename(ObjectMethod objectMethod, String newName) {
      requireNonNull(objectMethod);
      return new ObjectMethod() {
        @Override
        public String getName() {
          return newName;
        }

        @Override
        public int getParameterCount() {
          return objectMethod.getParameterCount();
        }

        @Override
        public Class<?>[] getParameterTypes() {
          return objectMethod.getParameterTypes();
        }

        @Override
        public Doc getParameterDoc(int index) {
          return objectMethod.getParameterDoc(index);
        }

        @Override
        public Doc doc() {
          return objectMethod.doc();
        }

        @Override
        public boolean isVarArgs() {
          return objectMethod.isVarArgs();
        }

        @Override
        public boolean isAccessor() {
          return objectMethod.isAccessor();
        }

        @Override
        public Object invoke(Object... args) {
          return objectMethod.invoke(args);
        }
      };
    }

    private String getMethodName(ObjectMethod method) {
      return method.getName();
    }

    private class FormImpl extends Base {
      private FormImpl(ObjectMethod objectMethod) {
        super(objectMethod);
      }

      @Override
      public Func apply(FuncInvoker funcInvoker, Arguments arguments) {
        Object[] args = toArray(
            stream(
                arguments.spliterator(), false
            ).map(
                statement -> statement.compile(funcInvoker)
            ).collect(
                toList()
            ),
            Func.class
        );
        if (requireNonNull(objectMethod).isVarArgs()) {
          int parameterCount = objectMethod.getParameterCount();
          args = Factory.this.shrinkTo(objectMethod.getParameterTypes()[parameterCount - 1].getComponentType(), parameterCount, args);
        }
        return funcFactory.create(funcInvoker, objectMethod, args);
      }
    }

    private static Stage createWrappedStage(Stage input, Func<?>[] args) {
      List<Object> argValues = Arrays.stream(args).map((Func each) -> each.apply(input)).collect(toList());
      return new Stage.Delegating(input) {
        @Override
        public <U> U getArgument(int index) {
          check(index < sizeOfArguments(), () -> indexOutOfBounds(index, sizeOfArguments()));
          //noinspection unchecked
          return (U) argValues.get(index);
        }

        @Override
        public int sizeOfArguments() {
          return argValues.size();
        }
      };
    }

    private class UserForm extends FormImpl {
      private final Supplier<List<Object>> userDefinedFormClauseSupplier;

      UserForm(ObjectMethod objectMethod, Supplier<List<Object>> userDefinedFormClauseSupplier) {
        super(objectMethod);
        this.userDefinedFormClauseSupplier = userDefinedFormClauseSupplier;
      }

      @Override
      public Func apply(FuncInvoker funcInvoker, Arguments arguments) {
        return super.apply(funcInvoker, new Arguments() {
          Iterable<Statement> statements = concat(
              ImmutableList.of(statementFactory.create(userDefinedFormClauseSupplier.get())),
              arguments
          );

          @SuppressWarnings("NullableProblems")
          @Override
          public Iterator<Statement> iterator() {
            return statements.iterator();
          }
        });
      }
    }

    private static class Lambda implements Form {
      @Override
      public Func apply(FuncInvoker funcInvoker, Arguments arguments) {
        Func ret = getOnlyElement(arguments).compile(funcInvoker);
        int i = 0;
        for (Statement each : arguments) {
          System.out.printf("args[%d]:%s%n", i++, each.format());
        }
        return ret;
      }

      @Override
      public boolean isAccessor() {
        return false;
      }

      private static Statement getOnlyElement(Arguments arguments) {
        Iterator<Statement> i = arguments.iterator();
        Statement ret = Exceptions.I.requireValue(Iterator::hasNext, i).next();
        Exceptions.I.requireValue(v -> !v.hasNext(), i);
        return ret;
      }
    }
  }
}
