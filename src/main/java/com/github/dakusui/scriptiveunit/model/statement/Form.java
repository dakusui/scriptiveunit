package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.func.Func;

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
  Func apply(Arguments arguments);

  boolean isAccessor();

  class Factory {
    private final Object              driver;
    private final Func.Factory        funcFactory;
    private final TestSuiteDescriptor testSuiteDescriptor;
    private final Statement.Factory   statementFactory;

    public Factory(TestSuiteDescriptor testSuiteDescriptor, Func.Factory funcFactory, Statement.Factory statementFactory) {
      this.testSuiteDescriptor = testSuiteDescriptor;
      this.driver = requireNonNull(testSuiteDescriptor.getDriverObject());
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
      List<Object> deformClause = testSuiteDescriptor.getUserDefinedFormClauses().get(name);
      if (deformClause == null)
        return null;
      return new UserForm(Factory.this.getObjectMethodFromDriver("userFunc"), deformClause);
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

    private String getMethodName(ObjectMethod method) {
      return method.getName();
    }

    private class Impl implements Form {
      private final ObjectMethod objectMethod;

      Impl(ObjectMethod objectMethod) {
        this.objectMethod = objectMethod;
      }

      @Override
      public Func apply(Arguments arguments) {
        Object[] args = toArray(stream(arguments.spliterator(), false)
            .map(Statement::execute)
            .collect(toList()), Func.class);
        if (requireNonNull(objectMethod).isVarArgs()) {
          int parameterCount = objectMethod.getParameterCount();
          args = Factory.this.shrinkTo(objectMethod.getParameterTypes()[parameterCount - 1].getComponentType(), parameterCount, args);
        }
        return funcFactory.create(objectMethod, args);
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
      public Func apply(Arguments arguments) {
        return super.apply(new Arguments() {
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
