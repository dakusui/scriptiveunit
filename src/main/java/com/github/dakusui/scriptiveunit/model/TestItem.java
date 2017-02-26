package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;

public interface TestItem {
  int getTestCaseId();

  Tuple getTestCaseTuple();

  int getTestOracleId();

  String getTestOracleDescription();

  int getTestItemId();

  static TestItem create(IndexedTestCase testCase, TestOracle testOracle, int testItemId) {
    return new TestItem() {
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
        return testOracle.getDescription();
      }

      @Override
      public int getTestItemId() {
        return testItemId;
      }
    };
  }
}
