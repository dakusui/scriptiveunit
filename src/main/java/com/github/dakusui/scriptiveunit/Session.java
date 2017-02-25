package com.github.dakusui.scriptiveunit;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestItem;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.Report;
import org.junit.runner.Runner;

import static java.util.Objects.requireNonNull;

public interface Session {
  Config getConfig();

  Report createReport(TestItem testItem);

  TestSuiteDescriptor getDescriptor();

  Iterable<Runner> createTestItemRunners();

  Stage createTopLevelStage();

  static Session create(TestSuiteDescriptor.Loader loader) {
    return new Impl(loader);
  }

  class Impl implements Session {
    private final Config                     config;
    private final TestSuiteDescriptor.Loader loader;
    private       TestSuiteDescriptor        testSuiteDescriptor;

    @SuppressWarnings("WeakerAccess")
    protected Impl(TestSuiteDescriptor.Loader loader) {
      this.loader = requireNonNull(loader);
      this.config = loader.getConfig();
    }

    @Override
    public Config getConfig() {
      return this.config;
    }

    @Override
    public Report createReport(TestItem testItem) {
      return Report.create(testSuiteDescriptor.getConfig(), testItem);
    }

    @Override
    synchronized public TestSuiteDescriptor getDescriptor() {
      if (this.testSuiteDescriptor == null) {
        this.testSuiteDescriptor = loader.loadTestSuiteDescriptor(this);
      }
      return this.testSuiteDescriptor;
    }

    @Override
    public Iterable<Runner> createTestItemRunners() {
      return getDescriptor().getRunnerType().createRunners(this);
    }

    @Override
    public Stage createTopLevelStage() {
      return Stage.Factory.createTopLevel(this);
    }
  }

}
