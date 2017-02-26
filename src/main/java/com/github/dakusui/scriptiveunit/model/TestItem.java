package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;

public interface TestItem {
  int getTestCaseId();

  Tuple getTestCaseTuple();

  int getTestOracleId();

  @ReflectivelyReferenced
  String getTestOracleDescription();

  int getTestItemId();

  class Impl implements TestItem {
    private final IndexedTestCase testCase;
    private final TestOracle      testOracle;
    private final int             testItemId;

    Impl(IndexedTestCase testCase, TestOracle testOracle, int testItemId) {
      this.testCase = testCase;
      this.testOracle = testOracle;
      this.testItemId = testItemId;
    }

    @Override
    public int getTestCaseId() {
      return testCase.getIndex();
    }

    @Override
    public Tuple getTestCaseTuple() {
      return testCase.getTuple();
    }

    @Override
    public int getTestOracleId() {
      return testOracle.getIndex();
    }

    @Override
    public String getTestOracleDescription() {
      return testOracle.templateDescription(testCase.getTuple(), "");
    }

    @Override
    public int getTestItemId() {
      return testItemId;
    }
  }

  static TestItem create(String testSuiteDescription, IndexedTestCase testCase, TestOracle testOracle, int testItemId) {
    return new Impl(testCase, testOracle, testItemId);
  }
}
