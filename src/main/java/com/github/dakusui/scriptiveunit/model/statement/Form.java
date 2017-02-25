package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.toArray;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public interface Form {
  Func apply(FuncInvoker funcInvoker, Arguments arguments);

  boolean isAccessor();

  class Factory {
    private final Object            driver;
    private final Func.Factory      funcFactory;
    private final Session           session;
    private final Statement.Factory statementFactory;

    public Factory(Session session, Func.Factory funcFactory, Statement.Factory statementFactory) {
      this.session = requireNonNull(session);
      this.driver = requireNonNull(session.getConfig().getDriverObject());
      this.funcFactory = funcFactory;
      this.statementFactory = statementFactory;
    }

    @SuppressWarnings("WeakerAccess")
    public Form create(String name) {
      ObjectMethod objectMethod = Factory.this.getObjectMethodFromDriver(name);
      return requireNonNull(objectMethod != null ?
          new Impl(objectMethod) :
          createUserForm(name), String.format("A form '%s' was not found", name));
    }

    private Form createUserForm(String name) {
      List<Object> userFormClause = session.getDescriptor().getUserDefinedFormClauses().get(name);
      if (userFormClause == null)
        return null;
      return new UserForm(rename(Factory.this.getObjectMethodFromDriver("userFunc"), name), userFormClause);
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

    private ObjectMethod getObjectMethodFromDriver(String methodName) {
      for (ObjectMethod each : ScriptiveUnit.getAnnotatedMethodsFromImportedFieldsInObject(this.driver)) {
        if (getMethodName(each).equals(methodName))
          return each;
      }
      return null;
    }

    private ObjectMethod rename(ObjectMethod objectMethod, String newName) {
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

    private class Impl implements Form {
      private final ObjectMethod objectMethod;

      Impl(ObjectMethod objectMethod) {
        this.objectMethod = objectMethod;
      }

      @Override
      public Func apply(FuncInvoker funcInvoker, Arguments arguments) {
        Object[] args = toArray(stream(arguments.spliterator(), false)
            .map(statement -> statement.execute(funcInvoker))
            .collect(toList()), Func.class);
        if (requireNonNull(objectMethod).isVarArgs()) {
          int parameterCount = objectMethod.getParameterCount();
          args = Factory.this.shrinkTo(objectMethod.getParameterTypes()[parameterCount - 1].getComponentType(), parameterCount, args);
        }
        return funcFactory.create(funcInvoker, objectMethod, args);
      }

      @Override
      public boolean isAccessor() {
        return this.objectMethod.isAccessor();
      }
    }

    private class UserForm extends Impl {
      private final List<Object> userDefinedFormClause;

      UserForm(ObjectMethod objectMethod, List<Object> userDefinedFormClause) {
        super(objectMethod);
        this.userDefinedFormClause = userDefinedFormClause;
      }

      @Override
      public Func apply(FuncInvoker funcInvoker, Arguments arguments) {
        return super.apply(funcInvoker, new Arguments() {
          Iterable<Statement> statements = concat(of(statementFactory.create(userDefinedFormClause)), arguments);

          @Override
          public Iterator<Statement> iterator() {
            return statements.iterator();
          }
        });
      }
    }
  }
}
