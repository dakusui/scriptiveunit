package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;

import java.util.Optional;

public interface StageFactory {
  static <RESPONSE> Stage oracleLevelStageFor(Stage.ExecutionLevel executionLevel, Config config, TestItem testItem, RESPONSE response, Throwable throwable, Report report) {
    return new OracleLevelStage<>(response, executionLevel, throwable, config, report, testItem);
  }

  static Stage frameworkStageFor(Stage.ExecutionLevel executionLevel, Config config, Tuple fixture) {
    return new FrameworkStage<>(fixture, executionLevel, config);
  }

  abstract class StageBase<RESPONSE> implements Stage {
    private final RESPONSE       response;
    private final ExecutionLevel executionLevel;
    private final Throwable      throwable;
    private final Config         config;
    private final Report         report;

    StageBase(RESPONSE response, ExecutionLevel executionLevel, Throwable throwable, Config config, Report report) {
      this.response = response;
      this.executionLevel = executionLevel;
      this.throwable = throwable;
      this.config = config;
      this.report = report;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<RESPONSE> response() {
      return Optional.ofNullable(response);
    }

    @Override
    public ExecutionLevel getExecutionLevel() {
      return executionLevel;
    }

    @Override
    public <T> T getArgument(int index) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int sizeOfArguments() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Throwable> getThrowable() {
      return Optional.ofNullable(throwable);
    }

    @Override
    public Config getConfig() {
      return config;
    }

    @Override
    public Optional<Report> getReport() {
      return Optional.ofNullable(report);
    }
  }

  class FrameworkStage<RESPONSE> extends StageBase<RESPONSE> {
    private final Tuple testCase;

    FrameworkStage(Tuple testCase, ExecutionLevel executionLevel, Config config) {
      super(null, executionLevel, null, config, null);
      this.testCase = testCase;
    }

    @Override
    public Optional<Tuple> getTestCaseTuple() {
      return Optional.of(testCase);
    }

    @Override
    public Optional<TestItem> getTestItem() {
      return Optional.empty();
    }
  }

  class OracleLevelStage<RESPONSE> extends StageBase<RESPONSE> {
    private final TestItem testItem;

    OracleLevelStage(RESPONSE response, ExecutionLevel executionLevel, Throwable throwable, Config config, Report report, TestItem testItem) {
      super(response, executionLevel, throwable, config, report);
      this.testItem = testItem;
    }

    @Override
    public Optional<Tuple> getTestCaseTuple() {
      return Optional.of(this.testItem.getTestCaseTuple());
    }

    @Override
    public Optional<TestItem> getTestItem() {
      return Optional.ofNullable(testItem);
    }
  }
}
