package com.github.dakusui.scriptiveunit.model.func;

import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.model.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Formattable;
import java.util.Formatter;

import static com.github.dakusui.scriptiveunit.core.Utils.check;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.fail;
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
      /*
       * By using dynamic proxy, we are making it possible to print structured pretty log.
       */
      return createFunc(
          objectMethod.getName(),
          (Func) check(
              returnedValue = objectMethod.invoke(args),
              (Object o) -> o instanceof Func,
              () -> valueReturnedByScriptableMethodMustBeFunc(objectMethod.getName(), returnedValue)
          ));
    }

    public <T> Func<?, T> createConst(T value) {
      return createProxy((proxy, method, args) -> funcHandler.handleConst(value), Const.class);
    }

    public <T> Func<? extends Stage, T> createArg(int index) {
      return new Func<Stage, T>() {
        @Override
        public T apply(Stage input) {
          return input.getArgument(index);
        }
      };
    }
    private Func createFunc(String name, Func target) {
      return createProxy(createInvocationHandler(name, target), Func.class);
    }

    private InvocationHandler createInvocationHandler(String name, Func target) {
      return (Object proxy, Method method, Object[] args) -> {
        check("apply".equals(method.getName()),
            fail("This should only be executed on 'apply' method.")
        );
        check(args.length == 1 && args[0] instanceof Stage,
            fail("The argument should be an array of length 1 and its first element should be '%s', but: %s",
                Arrays.toString(args),
                Stage.class.getCanonicalName()
            ));
        return funcHandler.handle(target, (Stage) args[0], name);
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
