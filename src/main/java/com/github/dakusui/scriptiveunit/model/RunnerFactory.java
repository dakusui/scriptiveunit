package com.github.dakusui.scriptiveunit.model;

import org.junit.runner.Runner;

public interface RunnerFactory {
  int numRunners();
  Runner createRunner(int runnerId, ActionFactory actionFactory);

  static RunnerFactory perTestCase(TestSuiteDescriptor descriptor) {
    return new RunnerFactory() {
      @Override
      public int numRunners() {
        return 0;
      }

      @Override
      public Runner createRunner(int runnerId, ActionFactory actionFactory) {
        return null;
      }
    };
  }

  static RunnerFactory perFixture(TestSuiteDescriptor descriptor) {
    return null;
  }
}
