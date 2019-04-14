package com.github.dakusui.scriptiveunit;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.Report;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestItem;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
import org.junit.runner.Runner;

public interface Session {
  Config getConfig();

  Report createReport(TestItem testItem);

  TestSuiteDescriptor getTestSuiteDescriptor();

  Iterable<Runner> createTestItemRunners();

  Stage createTopLevelStage();

  static Session create(TestSuiteDescriptor.Loader loader) {
    return new Impl(loader);
  }

  class Impl implements Session {
    private final Config              config;
    private final TestSuiteDescriptor testSuiteDescriptor;

    @SuppressWarnings("WeakerAccess")
    protected Impl(TestSuiteDescriptor.Loader loader) {
      this.config = loader.getConfig();
      this.testSuiteDescriptor = loader.loadTestSuiteDescriptor(this);
    }

    @Override
    public Config getConfig() {
      return this.config;
    }

    @Override
    public Report createReport(TestItem testItem) {
      return Report.create(getConfig(), testItem);
    }

    @Override
    synchronized public TestSuiteDescriptor getTestSuiteDescriptor() {
      return this.testSuiteDescriptor;
    }

    @Override
    public Iterable<Runner> createTestItemRunners() {
      return getTestSuiteDescriptor().getRunnerType().createRunners(this);
    }

    @Override
    public Stage createTopLevelStage() {
      return Stage.Factory.createTopLevel(this);
    }
  }

}
