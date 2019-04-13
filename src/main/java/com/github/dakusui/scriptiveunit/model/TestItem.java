package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;

public interface TestItem {
  int getTestCaseId();

  Tuple getTestCaseTuple();

  int getTestOracleId();

  @SuppressWarnings("unused")
  String getTestOracleDescription();

  class Impl implements TestItem {
    private final IndexedTestCase indexedTestCase;
    private final TestOracle      testOracle;
    private final String          testSuiteDescription;

    Impl(String testSuiteDescription, IndexedTestCase indexedTestCase, TestOracle testOracle) {
      this.indexedTestCase = indexedTestCase;
      this.testOracle = testOracle;
      this.testSuiteDescription = testSuiteDescription;
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

    @Override
    public String getTestOracleDescription() {
      return testOracle.templateDescription(indexedTestCase.get(), testSuiteDescription);
    }
  }

  static TestItem create(String testSuiteDescription, IndexedTestCase testCase, TestOracle testOracle) {
    return new Impl(testSuiteDescription, testCase, testOracle);
  }
}
