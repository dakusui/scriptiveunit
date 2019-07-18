package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.JsonScript;

import java.util.Optional;

class FrameworkStage extends StageBase {
  private final Tuple testCase;

  FrameworkStage(Tuple testCase, JsonScript script) {
    super(null, null, script, null);
    this.testCase = testCase;
  }

  @Override
  public Optional<Tuple> getTestCaseTuple() {
    return Optional.of(testCase);
  }

  @Override
  public String toString() {
    return String.format("stage:fixture:<%s>", testCase);
  }
}
