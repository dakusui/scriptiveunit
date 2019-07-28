package com.github.dakusui.scriptiveunit.model.stage;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.session.Report;

import java.util.Optional;

class OracleLevelStage extends Stage.Base {
  private final IndexedTestCase indexedTestCase;
  private final TestOracle      testOracle;

  OracleLevelStage(Object response, Throwable throwable, Script script, Report report, IndexedTestCase testCase, TestOracle testOracle) {
    super(response, throwable, script, report);
    this.indexedTestCase = testCase;
    this.testOracle = testOracle;
  }

  @Override
  public Optional<Tuple> getTestCaseTuple() {
    return Optional.of(this.indexedTestCase.getTestInput());
  }

  @Override
  public Optional<IndexedTestCase> getTestCase() {
    return Optional.of(this.indexedTestCase);
  }

  @Override
  public Optional<TestOracle> getTestOracle() {
    return Optional.of(this.testOracle);
  }

  @Override
  public String toString() {
    return String.format("stage:oracle:<%s>", indexedTestCase);
  }
}
