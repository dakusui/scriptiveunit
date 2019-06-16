package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.func.Form;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static java.util.Objects.requireNonNull;

public interface TestSuiteDescriptor {
  String getDescription();

  ScriptiveUnit.Mode getRunnerType();

  ParameterSpaceDescriptor getFactorSpaceDescriptor();

  Map<String, List<Object>> getUserDefinedFormClauses();

  List<? extends TestOracle> getTestOracles();

  List<IndexedTestCase> getTestCases();

  Form<Action> getSetUpBeforeAllActionFactory();

  Form<Action> getSetUpActionFactory();

  Form<Action> getTearDownActionFactory();

  Form<Action> getTearDownAfterAllActionFactory();

  List<String> getInvolvedParameterNamesInSetUpAction();

  Config getConfig();

  Statement.Factory statementFactory();

  interface Loader {
    Config getConfig();

    TestSuiteDescriptor loadTestSuiteDescriptor(Session session);

    abstract class Base implements Loader {

      private final Config config;

      public Base(Config config) {
        this.config = requireNonNull(config);
      }

      public Config getConfig() {
        return this.config;
      }
    }

    static Loader createInstance(Class<? extends Loader> klass, Config config) {
      try {
        return klass.getConstructor(Config.class).newInstance(config);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw wrap(e);
      }
    }
  }
}
