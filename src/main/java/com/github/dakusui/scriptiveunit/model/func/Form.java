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
public interface Form<O> extends
    java.util.function.Function<Stage, O>,
    Formattable {
  @Override
  O apply(Stage input);

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    try {
      formatter.out().append(this.name());
    } catch (IOException e) {
      throw wrap(e);
    }
  }

  default String name() {
    return "(noname)";
  }

  /**
   * An interface that represents a constant. The {@code apply} method of an
   * implementation of this class must return the same value always regardless
   * of its arguments value, even if a {@code null} is given.
   *
   * @param <O> Type of output constant.
   */
  interface Const<O> extends Form<O> {
  }

  class Factory {
    private final FuncHandler funcHandler;

    public Factory(FuncHandler funcHandler) {
      this.funcHandler = funcHandler;
    }

    /*
     * args is an array can only contain Form or Form[]. Only the last element in it
     * can become Form[] it is because only the last argument of a method can become
     * a varargs.
     */
    public Form create(FuncInvoker invoker, ObjectMethod objectMethod, Object[] args) {
      Object returnedValue;
      /*
       * By using dynamic proxy, we are making it possible to print structured pretty log.
       */
      return createFunc(
          invoker,
          objectMethod.getName(),
          (Form) check(
              returnedValue = objectMethod.invoke(args),
              (Object o) -> o instanceof Form,
              () -> valueReturnedByScriptableMethodMustBeFunc(objectMethod.getName(), returnedValue)
          ));
    }

    public <T> Form<T> createConst(FuncInvoker invoker, T value) {
      return createProxy((proxy, method, args) -> funcHandler.handleConst(invoker, value), Const.class);
    }

    private Form createFunc(FuncInvoker invoker, String name, Form target) {
      return createProxy(createInvocationHandler(invoker, name, target), Form.class);
    }

    private InvocationHandler createInvocationHandler(FuncInvoker invoker, String name, Form target) {
      return (Object proxy, Method method, Object[] args) -> {
        if (!"apply".equals(method.getName()))
          return method.invoke(target, args);
        check(args.length == 1 && args[0] instanceof Stage,
            fail("The argument should be an array of length 1 and its first element should be an instance of %s, but it was: %s",
                Stage.class.getCanonicalName(),
                Arrays.toString(args)
            ));
        return funcHandler.handle(invoker, target, (Stage) args[0], name);
      };
    }

    private static <O> Form<O> createProxy(InvocationHandler handler, Class<? extends Form> interfaceClass) {
      //noinspection unchecked
      return (Form<O>) Proxy.newProxyInstance(
          Form.class.getClassLoader(),
          new Class[] { interfaceClass },
          handler
      );
    }
  }
}
