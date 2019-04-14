package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.scriptiveunit.GroupedTestItemRunner;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import org.junit.runner.Runner;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static java.util.Objects.requireNonNull;

public interface TestSuiteDescriptor {
  String getDescription();

  GroupedTestItemRunner.Type getRunnerType();

  ParameterSpaceDescriptor getFactorSpaceDescriptor();

  Map<String, List<Object>> getUserDefinedFormClauses();

  List<? extends TestOracle> getTestOracles();

  List<IndexedTestCase> getTestCases();

  Func<Action> getSetUpBeforeAllActionFactory();

  Func<Action> getSetUpActionFactory();

  Func<Action> getTearDownActionFactory();

  Func<Action> getTearDownAfterAllActionFactory();

  List<String> getInvolvedParameterNamesInSetUpAction();

  Config getConfig();

  default Iterable<Runner> createRunners(Session session) {
    return this.getRunnerType().createRunners(session, this);
  }

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
