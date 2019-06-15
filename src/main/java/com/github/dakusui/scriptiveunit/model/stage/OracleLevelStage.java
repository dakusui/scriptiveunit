package com.github.dakusui.scriptiveunit.model.stage;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.Report;
import com.github.dakusui.scriptiveunit.model.TestItem;

import java.util.Optional;

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
