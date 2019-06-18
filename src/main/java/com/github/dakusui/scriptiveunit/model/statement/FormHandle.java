package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormInvoker;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.utils.DriverUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dakusui.scriptiveunit.core.Exceptions.SCRIPTIVEUNIT;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.toArray;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

interface FormHandle {
  static List<Form> toFuncs(FormInvoker formInvoker, Iterable<Statement> arguments) {
    return stream(arguments.spliterator(), false)
        .map(statement -> statement.compile(formInvoker))
        .collect(toList());
  }

  Form apply(FormInvoker formInvoker, Arguments arguments);

  boolean isAccessor();

  abstract class Base implements FormHandle {
    private final String name;

    Base(String name) {
      this.name = name;
    }

    public String name() {
      return this.name;
    }
  }

  enum Utils {
    ;

    static <T> T car(T[] arr) {
      return SCRIPTIVEUNIT.requireValue(v -> v.length > 0, SCRIPTIVEUNIT.requireNonNull(arr))[0];
    }

    static <T> T[] cdr(T[] arr) {
      return Arrays.copyOfRange(
          SCRIPTIVEUNIT.requireValue(v -> v.length > 0, SCRIPTIVEUNIT.requireNonNull(arr)),
          1,
          arr.length
      );
    }

  }

  class Factory {
    private final Object                    driver;
    private final Form.Factory formFactory;
    private final Statement.Factory         statementFactory;
    private final Map<String, List<Object>> clauseMap;

    Factory(Form.Factory formFactory, Statement.Factory statementFactory, Config config, Map<String, List<Object>> userDefinedFormClauses) {
      this.driver = requireNonNull(config.getDriverObject());
      this.formFactory = formFactory;
      this.statementFactory = statementFactory;
      this.clauseMap = requireNonNull(userDefinedFormClauses);
    }

    public FormHandle create(String name) {
      if ("lambda".equals(name))
        return new Lambda(name);
      return Factory.this.getObjectMethodFromDriver(name).map(
          (Function<ObjectMethod, FormHandle>) MethodBasedImpl::new
      ).orElseGet(
          () -> createUserForm(name)
      );
    }

    private FormHandle createUserForm(String name) {
      return new UserFormHandle(
          name,
          () -> statementFactory.create(
              getUserDefinedFormClauseFromSessionByName(name).orElseThrow(
                  () -> new NullPointerException(format("Undefined form '%s' was referenced.", name))
              ).get()
          )
      );
    }

    private static Form compile(Statement statement) {
      return statement.compile(FormInvoker.create(FormInvoker.createMemo()));
    }

    private Optional<Supplier<List<Object>>> getUserDefinedFormClauseFromSessionByName(String name) {
      return clauseMap.containsKey(name) ?
          Optional.of(() -> clauseMap.get(name)) :
          Optional.empty();
    }

    private Optional<ObjectMethod> getObjectMethodFromDriver(String methodName) {
      for (ObjectMethod each : DriverUtils.getObjectMethodsFromImportedFieldsInObject(this.driver)) {
        if (getMethodName(each).equals(methodName))
          return Optional.of(each);
      }
      return Optional.empty();
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

    private String getMethodName(ObjectMethod method) {
      return method.getName();
    }

    private class MethodBasedImpl extends Base {
      final ObjectMethod objectMethod;

      private MethodBasedImpl(ObjectMethod objectMethod) {
        super(objectMethod.getName());
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

      @Override
      public Form apply(FormInvoker formInvoker, Arguments arguments) {
        Form[] args = toArray(
            toFuncs(formInvoker, arguments),
            Form.class
        );
        // TODO a form doesn't need to know a FormInvoker with which it will be invoked.
        return createForm(formInvoker, args);
      }

      Form createForm(FormInvoker formInvoker, Form[] args) {
        Object[] argValues;
        if (requireNonNull(objectMethod).isVarArgs()) {
          int parameterCount = objectMethod.getParameterCount();
          argValues = Factory.this.shrinkTo(objectMethod.getParameterTypes()[parameterCount - 1].getComponentType(), parameterCount, args);
        } else
          argValues = args;
        return formFactory.create(formInvoker, objectMethod, argValues);
      }
    }

    private static class UserFormHandle extends Base {
      private final Supplier<Statement> userDefinedFormStatementSupplier;

      UserFormHandle(String name, Supplier<Statement> userDefinedFormStatementSupplier) {
        super(name);
        this.userDefinedFormStatementSupplier = userDefinedFormStatementSupplier;
      }

      @Override
      public Form<Object> apply(FormInvoker formInvoker, Arguments arguments) {
        return createFunc(
            toArray(
                Stream.concat(
                    Stream.of((Form<Statement>) input -> userDefinedFormStatementSupplier.get()),
                    toFuncs(formInvoker, arguments).stream()
                ).collect(toList()),
                Form.class
            )
        );
      }

      @Override
      public boolean isAccessor() {
        return false;
      }

      @SuppressWarnings("unchecked")
      Form<Object> createFunc(Form[] args) {
        return userFunc(Utils.car(args), Utils.cdr(args));
      }

      private static Form<Object> userFunc(Form<Statement> statementForm, Form<?>... args) {
        return (Stage input) -> compile(statementForm.apply(input)).apply(Stage.Factory.createWrappedStage(input, args));
      }
    }

    private static class Lambda extends Base {
      private Lambda(String name) {
        // TODO: need to consider how we should define a name for a lambda object
        super(name);
      }

      @SuppressWarnings("unchecked")
      @Override
      public Form<Form<Object>> apply(FormInvoker formInvoker, Arguments arguments) {
        return (Stage ii) -> getOnlyElement(toFuncs(formInvoker, arguments));
      }

      @Override
      public boolean isAccessor() {
        return false;
      }
    }
  }
}
