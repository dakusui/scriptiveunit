package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;

public interface TestItem {
  int getTestCaseId();

  Tuple getTestCaseTuple();

  int getTestOracleId();

  class Impl implements TestItem {
    private final IndexedTestCase indexedTestCase;
    private final TestOracle      testOracle;
    private String          testSuiteDescription;

    Impl(IndexedTestCase indexedTestCase, TestOracle testOracle) {
      this.indexedTestCase = indexedTestCase;
      this.testOracle = testOracle;
    }

    @Override
    public int getTestCaseId() {
      return indexedTestCase.getIndex();
    }

    @Override
    public Tuple getTestCaseTuple() {
      return indexedTestCase.get();
    }

    @Override
    public int getTestOracleId() {
      return testOracle.getIndex();
    }
  }

  static TestItem create(IndexedTestCase testCase, TestOracle testOracle) {
    return new Impl(testCase, testOracle);
  }
}
