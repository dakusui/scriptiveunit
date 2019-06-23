package com.github.dakusui.scriptiveunit.model.desc.testitem;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import static com.github.dakusui.scriptiveunit.utils.TupleUtils.append;
import static com.github.dakusui.scriptiveunit.utils.StringUtils.template;

public interface TestOracle {
  static String templateTestOracleDescription(TestOracle testOracle, Tuple testCaseTuple, String testSuiteDescription) {
    return template(testOracle.getDescription(), append(testCaseTuple, "@TESTSUITE", testSuiteDescription));
  }

  int getIndex();

  /**
   * Returns a string that describes this test oracle.
   * <p>
   * Note that this method always returns raw form (a string before being templated).
   */
  String getDescription();

  String templateDescription(Tuple testCaseTuple, String testSuiteDescription);

  TestOracleActionFactory testOracleActionFactoryFor(TestItem testItem);
}
