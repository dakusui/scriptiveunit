package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type;
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

    public Base(String scriptResourceName, Class<?> driverClass) {
      this.scriptResourceName = scriptResourceName;
      this.testSuiteDescriptor = loadTestSuiteDescriptor(driverClass, scriptResourceName);
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

    abstract protected TestSuiteDescriptor loadTestSuiteDescriptor(Class<?> driverClass, String resourceName);
  }

  interface Factory {
    TestSuiteLoader create(String resourceName, Class<?> driverClass);

    static Factory create(Class<? extends Factory> klass) {
      try {
        return Utils.getConstructor(klass).newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw wrap(e);
      }
    }
  }
}