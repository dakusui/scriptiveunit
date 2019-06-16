package com.github.dakusui.scriptiveunit.model.desc;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.Session;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;

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

  interface TestSuiteDescriptorLoader {
    Config getConfig();

    TestSuiteDescriptor loadTestSuiteDescriptor(Session session);

    abstract class Base implements TestSuiteDescriptorLoader {

      private final Config config;

      public Base(Config config) {
        this.config = requireNonNull(config);
      }

      public Config getConfig() {
        return this.config;
      }
    }

    static TestSuiteDescriptorLoader createInstance(Class<? extends TestSuiteDescriptorLoader> klass, Config config) {
      try {
        return klass.getConstructor(Config.class).newInstance(config);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw wrap(e);
      }
    }
  }
}
