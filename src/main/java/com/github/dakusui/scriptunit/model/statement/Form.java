package com.github.dakusui.scriptunit.model.statement;

import com.github.dakusui.scriptunit.ScriptUnit;
import com.github.dakusui.scriptunit.core.ObjectMethod;
import com.github.dakusui.scriptunit.model.Func;

import java.lang.reflect.Array;

import static com.google.common.collect.Iterables.toArray;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public interface Form {
  Object apply(Arguments arguments);

  class Factory {
    private final Object driver;

    /**
     * @param driver Already validated drivers object.
     */
    public Factory(Object driver) {
      this.driver = requireNonNull(driver);
    }

    @SuppressWarnings("WeakerAccess")
    public Form create(String name, Func.Invoker funcInvoker) {
      return arguments -> {
        ObjectMethod method = getMethodFromDriver(name);
        Object[] args = toArray(stream(arguments.spliterator(), false)
            .map(Statement::execute)
            .collect(toList()), Object.class);
        if (method.isVarArgs()) {
          int parameterCount = method.getParameterCount();
          args = shrinkTo(method.getParameterTypes()[parameterCount - 1].getComponentType(), parameterCount, args);
        }
        return funcInvoker.create(method, args);
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
