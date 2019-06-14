package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;

import java.util.Optional;

public interface StageFactory {
  static <RESPONSE> Stage oracleLevelStageFor(Stage.Type type, Config config, TestItem testItem, RESPONSE response, Throwable throwable, Report report) {
    return new OracleLevelStage<>(response, type, throwable, config, report, testItem);
  }

  static Stage frameworkStageFor(Stage.Type type, Config config, Tuple fixture) {
    return new FrameworkStage<>(fixture, type, config);
  }

  abstract class StageBase<RESPONSE> implements Stage {
    private final RESPONSE  response;
    private final Type      type;
    private final Throwable throwable;
    private final Config    config;
    private final Report    report;

    StageBase(RESPONSE response, Type type, Throwable throwable, Config config, Report report) {
      this.response = response;
      this.type = type;
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
    public Type getType() {
      return type;
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

    FrameworkStage(Tuple testCase, Type type, Config config) {
      super(null, type, null, config, null);
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

    OracleLevelStage(RESPONSE response, Type type, Throwable throwable, Config config, Report report, TestItem testItem) {
      super(response, type, throwable, config, report);
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
