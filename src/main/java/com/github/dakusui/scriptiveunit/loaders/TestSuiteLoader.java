package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;

import java.lang.reflect.InvocationTargetException;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;

public interface TestSuiteLoader {
  String getScriptResourceName();

  Type getRunnerType();

  TestSuiteDescriptor getTestSuiteDescriptor();

  abstract class Base implements TestSuiteLoader {

    private final TestSuiteDescriptor testSuiteDescriptor;
    private final String              scriptResourceName;
    private final Config              config;

    public Base(Config config) {
      this.config = config;
      this.scriptResourceName = config.getScriptResourceName();
      this.testSuiteDescriptor = loadTestSuiteDescriptor(config);
    }

    @Override
    public String getScriptResourceName() {
      return this.scriptResourceName;
    }

    @Override
    public Type getRunnerType() {
      return this.testSuiteDescriptor.getRunnerType();
    }

    @Override
    public TestSuiteDescriptor getTestSuiteDescriptor() {
      return this.testSuiteDescriptor;
    }

    abstract protected TestSuiteDescriptor loadTestSuiteDescriptor(Config config);
  }

  interface Factory {
    TestSuiteLoader create(Config config);

    static Factory create(Class<? extends Factory> klass) {
      try {
        return Utils.getConstructor(klass).newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw wrap(e);
      }
    }
  }
}