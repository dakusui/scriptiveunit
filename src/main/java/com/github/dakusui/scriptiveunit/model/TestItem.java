package com.github.dakusui.scriptiveunit.model;

public interface TestItem {
  int getTestCaseId();

  int getTestOracleId();

  int getTestItemId();

  static TestItem create(int testCaseId, int testOracleId, int testItemId) {
    return new TestItem() {
      @Override
      public int getTestCaseId() {
        return testCaseId;
      }

      @Override
      public int getTestOracleId() {
        return testOracleId;
      }

      @Override
      public int getTestItemId() {
        return testItemId;
      }
    };
  }
}
