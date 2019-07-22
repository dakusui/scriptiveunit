package com.github.dakusui.scriptiveunit.model.stage;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;

import java.util.Optional;

class FrameworkStage extends Stage.Base {
  private final Tuple testCase;

  FrameworkStage(Tuple testCase, Script script) {
    super(null, null, script, null);
    this.testCase = testCase;
  }

  @Override
  public Optional<Tuple> getTestCaseTuple() {
    return Optional.of(testCase);
  }

  @Override
  public Optional<IndexedTestCase> getTestCase() {
    return Optional.empty();
  }

  @Override
  public Optional<TestOracle> getTestOracle() {
    return Optional.empty();
  }

  @Override
  public String toString() {
    return String.format("stage:fixture:<%s>", testCase);
  }
}
