package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.model.session.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.utils.Checks.check;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.fail;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.exceptions.TypeMismatch.valueReturnedByScriptableMethodMustBeFunc;

@FunctionalInterface
public interface Form<O> extends Function<Stage, O>, Formattable {
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

    public Factory() {
    }

    /*
     * args is an array can only contain Form or Form[]. Only the last element in it
     * can become Form[] it is because only the last argument of a method can become
     * a varargs.
     */
    public Form create(ObjectMethod objectMethod, Object[] args) {
      Object returnedValue;
      /*
       * By using dynamic proxy, we are making it possible to print structured pretty log.
       */
      return createForm(
          objectMethod.getName(),
          (Form) check(
              returnedValue = objectMethod.invoke(args),
              (Object o) -> o instanceof Form,
              () -> valueReturnedByScriptableMethodMustBeFunc(objectMethod.getName(), returnedValue)
          ));
    }

    public <T> Form<T> createConst(T value) {
      return createProxy(
          (proxy, method, args) -> FormInvoker.Utils.invokeConst(value),
          Const.class);
    }

    private Form createForm(String name, Form target) {
      return createProxy(createInvocationHandler(name, target), Form.class);
    }

    private InvocationHandler createInvocationHandler(String name, Form target) {
      return (Object proxy, Method method, Object[] args) -> {
        if (!"apply".equals(method.getName()))
          return method.invoke(target, args);
        //MEMOIZATION SHOULD HAPPEN HERE
        check(args.length == 1 && args[0] instanceof Stage,
            fail("The argument should be an array of length 1 and its first element should be an instance of %s, but it was: %s",
                Stage.class.getCanonicalName(),
                Arrays.toString(args)
            ));
        //MEMOIZATION SHOULD HAPPEN HERE
        return FormInvoker.Utils.invokeForm(target, (Stage) args[0], name);
        //return formHandler.handleForm(invoker, target, (Stage) args[0], name);
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
