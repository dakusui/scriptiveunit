package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;

import java.util.Optional;

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
