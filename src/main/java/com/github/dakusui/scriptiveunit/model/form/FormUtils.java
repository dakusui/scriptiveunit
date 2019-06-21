package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.FormHandle;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.utils.CoreUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.fail;
import static com.github.dakusui.scriptiveunit.exceptions.TypeMismatch.valueReturnedByScriptableMethodMustBeFunc;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.toArray;
import static java.util.stream.Collectors.toList;

public enum FormUtils {
  ;

  public static <U> Form<U> toForm(Statement statement) {
    if (statement instanceof Statement.Atom) {
      Statement.Atom atom = (Statement.Atom) statement;
      if (atom.isParameterAccessor()) {
        return input -> input.getArgument((atom.value()));
      } else {
        return createConst(atom.value());
      }
    }
    if (statement instanceof Statement.Compound) {
      Statement.Compound compound = (Statement.Compound) statement;
      FormHandle formHandle = compound.getFormHandle();
      if (formHandle instanceof FormHandle.MethodBased) {
        Form[] args = toArray(
            FormHandle.toForms(compound.getArguments()),
            Form.class
        );
        // TODO a form doesn't need to know a FormInvoker with which it will be invoked.
        Object[] argValues;
        FormHandle.MethodBased methodBased = (FormHandle.MethodBased) formHandle;
        ObjectMethod objectMethod = methodBased.objectMethod();
        if (objectMethod.isVarArgs()) {
          int parameterCount = objectMethod.getParameterCount();
          argValues = CoreUtils.shrinkTo(objectMethod.getParameterTypes()[parameterCount - 1].getComponentType(), parameterCount, args);
        } else
          argValues = args;
        return createFormForCompoundStatement(objectMethod, argValues);
      }
      if (formHandle instanceof FormHandle.User)
        //noinspection unchecked
        return (Form<U>) createFunc(
            toArray(
                Stream.concat(
                    Stream.of((Form<Statement>) input -> {
                      FormHandle.User userFormHandle = (FormHandle.User) formHandle;
                      return userFormHandle.userDefinedFormStatementSupplier.get();
                    }),
                    FormHandle.toForms(compound.getArguments()).stream()
                ).collect(toList()),
                Form.class
            )
        );
      if (formHandle instanceof FormHandle.Lambda)
        //noinspection unchecked
        return (Form<U>) (Form<Form<Object>>) (Stage ii) -> getOnlyElement(FormHandle.toForms(compound.getArguments()));
      throw new IllegalArgumentException();
    }
    throw new IllegalArgumentException();
  }

  @SuppressWarnings("unchecked")
  private static Form<Object> createFunc(Form[] args) {
    return FormHandle.User.userFunc(CoreUtils.car(args), CoreUtils.cdr(args));
  }

  /*
   * args is an array can only contain Form or Form[]. Only the last element in it
   * can become Form[] it is because only the last argument of a method can become
   * a varargs.
   */
  static <V> Form<V> createFormForCompoundStatement(ObjectMethod objectMethod, Object[] args) {
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

  static <T> Form<T> createConst(T value) {
    return createProxy(
        (proxy, method, args) -> FormInvoker.Utils.invokeConst(value),
        Form.Const.class);
  }

  private static <V> Form<V> createForm(String name, Form target) {
    return createProxy(createInvocationHandler(name, target), Form.class);
  }

  private static InvocationHandler createInvocationHandler(String name, Form target) {
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

  static <O> Form<O> createProxy(InvocationHandler handler, Class<? extends Form> interfaceClass) {
    //noinspection unchecked
    return (Form<O>) Proxy.newProxyInstance(
        Form.class.getClassLoader(),
        new Class[]{interfaceClass},
        handler
    );
  }
}
