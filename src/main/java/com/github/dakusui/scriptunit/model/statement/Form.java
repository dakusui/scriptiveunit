package com.github.dakusui.scriptunit.model.statement;

import com.github.dakusui.scriptunit.ScriptUnit;
import com.github.dakusui.scriptunit.core.ObjectMethod;
import com.github.dakusui.scriptunit.model.func.Func;
import com.github.dakusui.scriptunit.model.func.FuncHandler;

import java.lang.reflect.Array;

import static com.google.common.collect.Iterables.toArray;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public interface Form {
  Object apply(Arguments arguments);

  boolean isAccessor();

  class Factory {
    private final Object       driver;
    private final Func.Factory funcFactory;

    /**
     * @param driver Already validated drivers object.
     */
    public Factory(Object driver, Func.Factory funcFactory) {
      this.driver = requireNonNull(driver);
      this.funcFactory = funcFactory;
    }

    @SuppressWarnings("WeakerAccess")
    public Form create(String name, FuncHandler funcHandler) { /* TODO */
      ObjectMethod method = Factory.this.getMethodFromDriver(name);
      return new Form() {
        @Override
        public Object apply(Arguments arguments) {
          Object[] args = toArray(stream(arguments.spliterator(), false)
              .map(Statement::execute_)
              .collect(toList()), Object.class);
          if (method.isVarArgs()) {
            int parameterCount = method.getParameterCount();
            args = Factory.this.shrinkTo(method.getParameterTypes()[parameterCount - 1].getComponentType(), parameterCount, args);
          }
          return funcFactory.create(method, args);
        }

        @Override
        public boolean isAccessor() {
          return  Factory.this.getMethodFromDriver(name).isAccessor();
        }
      };
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

    private ObjectMethod getMethodFromDriver(String methodName) {
      for (ObjectMethod each : ScriptUnit.getAnnotatedMethodsFromImportedFieldsInObject(this.driver)) {
        if (getMethodName(each).equals(methodName))
          return each;
      }
      throw new RuntimeException(format("method '%s' annotated with 'Scriptable' was not found in '%s'", methodName, this.driver.getClass().getCanonicalName()));
    }

    private String getMethodName(ObjectMethod method) {
      return method.getName();
    }

  }
}
