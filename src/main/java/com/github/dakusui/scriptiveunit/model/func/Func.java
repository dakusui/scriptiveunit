package com.github.dakusui.scriptiveunit.model.func;

import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Formattable;
import java.util.Formatter;

import static com.github.dakusui.scriptiveunit.core.Utils.check;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.exceptions.TypeMismatch.valueReturnedByScriptableMethodMustBeFunc;

@FunctionalInterface
public interface Func<I, O> extends
    java.util.function.Function<I, O>,
    com.google.common.base.Function<I, O>,
    Formattable {
  @Override
  O apply(I input);

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    try {
      formatter.out().append("(unprintable)");
    } catch (IOException e) {
      throw wrap(e);
    }
  }

  interface Memoized<I, O> extends Func<I, O> {
  }

  /**
   * An interface that represents a constant. The {@code apply} method of an
   * implementation of this class must return the same value always regardless
   * of its arguments value, even if a {@code null} is given.
   *
   * @param <O> Type of output constant.
   */
  interface Const<O> extends Func<Object, O> {
  }

  class Factory {
    private final FuncHandler funcHandler;

    public Factory(FuncHandler funcHandler) {
      this.funcHandler = funcHandler;
    }

    public Func create(ObjectMethod objectMethod, Object[] args) {
      Object returnedValue;
      return createFunc(
          objectMethod.getName(),
          check(
              returnedValue = objectMethod.invoke(args),
              (Object o) -> o instanceof Func,
              () -> valueReturnedByScriptableMethodMustBeFunc(objectMethod.getName(), returnedValue)
          ));
    }

    public <T> Func<?, T> createConst(T value) {
      return createProxy((proxy, method, args) -> funcHandler.handleConst(value), Const.class);
    }

    private Func createFunc(String name, Object target) {
      return createProxy(createInvocationHandler(name, target), Func.class);
    }

    private InvocationHandler createInvocationHandler(String name, Object target) {
      return (Object proxy, Method method, Object[] args) -> {
        check("apply".equals(method.getName()), () -> {
          throw new ScriptiveUnitException("");
        });
        return funcHandler.invoke(target, method, args, name);
      };
    }

    private static <I, O> Func<I, O> createProxy(InvocationHandler handler, Class<? extends Func> interfaceClass) {
      //noinspection unchecked
      return (Func<I, O>) Proxy.newProxyInstance(
          Func.class.getClassLoader(),
          new Class[] { interfaceClass },
          handler
      );
    }
  }
}
