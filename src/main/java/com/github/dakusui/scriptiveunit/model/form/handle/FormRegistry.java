package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.drivers.actions.Basic;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.Report;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Arguments;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.utils.DriverUtils;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.dakusui.scriptiveunit.model.form.handle.FormRegistry.Utils.toArgs;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public interface FormRegistry {
  Optional<Form> lookUp(FormHandle handle);

  class Loader {
    private final Map<String, Form> formMap = new HashMap<>();


    Loader register(Object libraryObject) {
      requireNonNull(libraryObject);
      List<ObjectMethod> methods = DriverUtils.getObjectMethodsFromImportedFieldsInObject(libraryObject);
      for (ObjectMethod each : methods) {
        register(each, loadForm(each));
      }
      return this;
    }

    void register(ObjectMethod method, Form form) {
      if (formMap.containsKey(method.getName()))
        throw new IllegalStateException(
            format("Tried to register:%s with the key:%s, but %s was already registered.", form, form.name(), formMap.get(form)));
      formMap.put(method.getName(), form);
    }

    FormRegistry load() {
      return (FormHandle handle) -> Optional.ofNullable(formMap.get(handle.name()));
    }

    /**
     * @param method A method
     * @return A created form created from the given method object.
     */
    Form loadForm(ObjectMethod method) {
      return (Form) method.invoke((Object[]) toArgs(Utils.ensureAllFormClasses(method.getParameterTypes())));
    }

    public static void main(String... args) {
      FormRegistry formRegistry = new FormRegistry.Loader().register(new Basic()).load();
      Statement.Compound statement = new Statement.Compound() {
        @Override
        public FormHandle getFormHandle() {
          return null;
        }

        @Override
        public Arguments getArguments() {
          return null;
        }

        @Override
        public <V> V evaluate(Stage stage) {
          return null;
        }
      };
      Stage stage = createStage(formRegistry, statement);
    }

    private static Stage createStage(FormRegistry formRegistry, Statement.Compound statement) {
      return new Stage() {
        @Override
        public ExecutionLevel getExecutionLevel() {
          return ExecutionLevel.ORACLE;
        }

        @Override
        public Config getConfig() {
          return new Config.Builder(Object.class, System.getProperties()).build();
        }

        @Override
        public int sizeOfArguments() {
          return 0;
        }

        @Override
        public <T> T getArgument(int index) {
          throw new ArrayIndexOutOfBoundsException();
        }

        @Override
        public Optional<Throwable> getThrowable() {
          return Optional.empty();
        }

        @Override
        public Optional<Tuple> getTestCaseTuple() {
          return Optional.empty();
        }

        @Override
        public <RESPONSE> Optional<RESPONSE> response() {
          return Optional.empty();
        }

        @Override
        public Optional<Report> getReport() {
          return Optional.empty();
        }

        @Override
        public Optional<TestItem> getTestItem() {
          return Optional.empty();
        }

        @Override
        public FormRegistry formRegistry() {
          return formRegistry;
        }

        @Override
        public Statement.Compound ongoingStatement() {
          if (statement == null)
            throw new IllegalStateException();
          return statement;
        }
      };
    }
  }

  enum Utils {
    ;

    static Form[] toArgs(Class<Form>[] params) {
      return new ArrayList<Form>(params.length) {
        {
          for (int i = 0; i < params.length; i++) {
            this.add(createProxiedFormForArgumentAt(this.size()));
          }
        }
      }.toArray(new Form[params.length]);
    }

    private static Form createProxiedFormForArgumentAt(int i) {
      return (Form) Proxy.newProxyInstance(
          Form.class.getClassLoader(),
          new Class[]{Form.class},
          (proxy, method, args) -> {
            Stage stage = (Stage) requireThat(args, arrayLengthIsOneAndTheElementIsStage(), otherwiseThrowRuntimeException())[0];
            Arguments arguments = stage.ongoingStatement().getArguments();
            Statement statement = arguments.get(i);
            return statement.evaluate(stage);
          });
    }

    @SuppressWarnings("unchecked")
    private static Class<Form>[] ensureAllFormClasses(Class<?>[] parameterTypes) {
      for (Class<?> each : parameterTypes)
        requireThat(each, isAssignableFrom(Form.class), otherwiseThrowRuntimeException());
      return (Class<Form>[]) parameterTypes;
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> Predicate<Class<?>> isAssignableFrom(Class<T> formClass) {
      return formClass::isAssignableFrom;
    }

    private static Predicate<Object[]> arrayLengthIsOneAndTheElementIsStage() {
      return args -> args.length == 1 && args[0] instanceof Stage;
    }

    private static <V> BiFunction<V, Predicate<V>, RuntimeException> otherwiseThrowRuntimeException() {
      return (V v, Predicate<V> requirement) -> {
        throw new RuntimeException(format("Given value:'%s' did not satisfy the requirement:'%s'", v, requirement));
      };
    }

    private static <V> V requireThat(V value, Predicate<V> requirement, BiFunction<V, Predicate<V>, RuntimeException> otherwiseThrow) {
      if (requirement.test(value))
        return value;
      throw otherwiseThrow.apply(value, requirement);
    }

    public static Supplier<RuntimeException> undefinedFormError(Statement.Compound compoundStatement) {
      return () -> {
        throw new UnsupportedOperationException(format("Undefined form:'%s' was requested", compoundStatement.getFormHandle()));
      };
    }
  }
}
