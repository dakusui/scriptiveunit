package com.github.dakusui.scriptiveunit.model.desc.testitem;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.session.TestOracleValuesFactory;

import java.util.Optional;
import java.util.function.Function;

public interface TestItem {
  Optional<String> getDescription();

  int getTestCaseId();

  Tuple getTestCaseTuple();

  int getTestOracleId();

  TestOracleValuesFactory testOracleActionFactory(Function<Tuple, String> testCaseFormatter);

  class Impl implements TestItem {
    private final IndexedTestCase indexedTestCase;
    private final TestOracle      testOracle;

    Impl(IndexedTestCase indexedTestCase, TestOracle testOracle) {
      this.indexedTestCase = indexedTestCase;
      this.testOracle = testOracle;
    }

    @Override
    public Optional<String> getDescription() {
      return this.testOracle.getDescription();
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
    public TestOracleValuesFactory testOracleActionFactory(Function<Tuple, String> testCaseFormatter) {
      return TestOracleValuesFactory.createTestOracleValuesFactory(
          this,
          this.testOracle.definition(),
          testCaseFormatter
      );
    }
  }

  static TestItem create(IndexedTestCase testCase, TestOracle testOracle) {
    return new Impl(testCase, testOracle);
  }
}
