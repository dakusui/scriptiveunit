package com.github.dakusui.scriptiveunit.model.stage;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.session.Report;

import java.util.Optional;

class OracleLevelStage extends Stage.Base {
  private final TestItem testItem;

  OracleLevelStage(Object response, Throwable throwable, Script script, Report report, TestItem testItem) {
    super(response, throwable, script, report);
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

  @Override
  public String toString() {
    return String.format("stage:oracle:<%s>", testItem);
  }
}
