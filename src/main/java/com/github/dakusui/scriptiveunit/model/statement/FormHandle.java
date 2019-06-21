package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.model.form.FormUtils;
import com.github.dakusui.scriptiveunit.model.form.Form;
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

public interface FormHandle {
  static List<Form> toForms(Iterable<Statement> arguments) {
    return stream(arguments.spliterator(), false)
        .map(FormUtils::toForm)
        .collect(toList());
  }

  boolean isAccessor();

  default String name() {
    throw new UnsupportedOperationException();
  }

  abstract class Base implements FormHandle {
    private final String name;

    Base(String name) {
      this.name = name;
    }

    @Override
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
    private final Object driver;
    private final Form.Factory formFactory;
    private final Statement.Factory statementFactory;
    private final Map<String, List<Object>> clauseMap;

    Factory(Form.Factory formFactory, Statement.Factory statementFactory, Config config, Map<String, List<Object>> userDefinedFormClauses) {
      this.driver = requireNonNull(config.getDriverObject());
      this.formFactory = formFactory;
      this.statementFactory = statementFactory;
      this.clauseMap = requireNonNull(userDefinedFormClauses);
    }

    public FormHandle create(String name) {
      if ("lambda".equals(name))
        return new FormHandle.Lambda(name);
      return Factory.this.getObjectMethodFromDriver(name).map(
          (Function<ObjectMethod, FormHandle>) objectMethod -> new FormHandle.MethodBasedImpl(objectMethod, this, formFactory)
      ).orElseGet(
          () -> createUserForm(name)
      );
    }

    private FormHandle createUserForm(String name) {
      return new FormHandle.UserFormHandle(
          name,
          () -> statementFactory.create(
              getUserDefinedFormClauseFromSessionByName(name).orElseThrow(
                  () -> new NullPointerException(format("Undefined form '%s' was referenced.", name))
              ).get()
          )
      );
    }

    private static Form compile(Statement statement) {
      return FormUtils.toForm(statement);
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

  }

  class MethodBasedImpl extends Base {
    final ObjectMethod objectMethod;
    private final Factory formHandleFactory;
    private final Form.Factory formFactory;

    private MethodBasedImpl(ObjectMethod objectMethod, Factory formFactory, Form.Factory formFactory1) {
      super(objectMethod.getName());
      this.objectMethod = objectMethod;
      this.formHandleFactory = formFactory;
      this.formFactory = formFactory1;
    }

    @Override
    public boolean isAccessor() {
      return this.objectMethod.isAccessor();
    }

    @Override
    public String toString() {
      return String.format("form:%s", this.objectMethod);
    }

    public <V> Form<V> createForm(Form[] args) {
      Object[] argValues;
      if (requireNonNull(objectMethod).isVarArgs()) {
        int parameterCount = objectMethod.getParameterCount();
        argValues = formHandleFactory.shrinkTo(objectMethod.getParameterTypes()[parameterCount - 1].getComponentType(), parameterCount, args);
      } else
        argValues = args;
      return formFactory.create(objectMethod, argValues);
    }
  }

  class Lambda extends Base {
    private Lambda(String name) {
      // TODO: need to consider how we should define a name for a lambda object
      super(name);
    }

    @SuppressWarnings("unchecked")
    public Form<Form<Object>> apply(Arguments arguments) {
      // CAUTION: This method returns an instance of Form<Form<Object>>
      return (Stage ii) -> getOnlyElement(toForms(arguments));
    }

    @Override
    public boolean isAccessor() {
      return false;
    }
  }

  class UserFormHandle extends Base {
    public final Supplier<Statement> userDefinedFormStatementSupplier;

    UserFormHandle(String name, Supplier<Statement> userDefinedFormStatementSupplier) {
      super(name);
      this.userDefinedFormStatementSupplier = userDefinedFormStatementSupplier;
    }

    @Override
    public boolean isAccessor() {
      return false;
    }

    @SuppressWarnings("unchecked")
    public Form<Object> createFunc(Form[] args) {
      return userFunc(Utils.car(args), Utils.cdr(args));
    }

    private static Form<Object> userFunc(Form<Statement> statementForm, Form<?>... args) {
      return (Stage input) -> Factory.compile(statementForm.apply(input)).apply(Stage.Factory.createWrappedStage(input, args));
    }
  }
}
