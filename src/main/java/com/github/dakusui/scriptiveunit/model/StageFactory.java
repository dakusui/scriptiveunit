package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface StageFactory {
  static <RESPONSE> Stage _create(Stage.Type type, Config config, TestItem testItem, RESPONSE response, Throwable throwable, Report report) {
    return new MyStage<>(response, type, throwable, config, report, testItem);
  }

  static <RESPONSE> Stage _create2(Stage.Type type, Config config, Tuple testCase, RESPONSE response, Throwable throwable, Report report) {
    return new MyStage2<>(testCase, response, type, throwable, config, report);
  }

  abstract class MyStageBase<RESPONSE> implements Stage {
    private final RESPONSE               response;
    private final Type                   type;
    private final Throwable              throwable;
    private final Config                 config;
    private final Report                 report;
    private final Map<Func.Call, Object> memo;

    MyStageBase(RESPONSE response, Type type, Throwable throwable, Config config, Report report) {
      System.out.println(type);
      this.response = response;
      this.type = type;
      this.throwable = throwable;
      this.config = config;
      this.report = report;
      this.memo = new HashMap<>();
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

    @Override
    public Map<Func.Call, Object> memo() {
      return memo;
    }
  }

  class MyStage2<RESPONSE> extends MyStageBase<RESPONSE> {
    private final Tuple testCase;

    MyStage2(Tuple testCase, RESPONSE response, Type type, Throwable throwable, Config config, Report report) {
      super(response, type, throwable, config, report);
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

  class MyStage<RESPONSE> extends MyStage2<RESPONSE> {

    private final TestItem testItem;

    MyStage(RESPONSE response, Type type, Throwable throwable, Config config, Report report, TestItem testItem) {
      super(testItem.getTestCaseTuple(), response, type, throwable, config, report);
      this.testItem = testItem;
    }

    @Override
    public Optional<TestItem> getTestItem() {
      return Optional.ofNullable(testItem);
    }
  }
}
