package com.github.dakusui.scriptiveunit.model.desc.testitem;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.session.TestOracleFormFactory;

import java.util.function.Function;

public interface TestItem {
  int getTestCaseId();

  Tuple getTestCaseTuple();

  int getTestOracleId();

  TestOracleFormFactory testOracleActionFactory(Function<Tuple, String> testCaseFormatter);

  class Impl implements TestItem {
    private final IndexedTestCase indexedTestCase;
    private final TestOracle      testOracle;

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

    @Override
    public TestOracleFormFactory testOracleActionFactory(Function<Tuple, String> testCaseFormatter) {
      return TestOracleFormFactory.createTestOracleFormFactory(
          this,
          this.testOracle.definitionFor(this),
          testCaseFormatter
          );
    }
  }

  static TestItem create(IndexedTestCase testCase, TestOracle testOracle) {
    return new Impl(testCase, testOracle);
  }
}
