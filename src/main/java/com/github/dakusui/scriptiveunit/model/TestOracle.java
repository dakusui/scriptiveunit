package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.Session;

import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.core.Utils.append;
import static com.github.dakusui.scriptiveunit.core.Utils.template;

public interface TestOracle {
  static String templateTestOracleDescription(TestOracle testOracle, Tuple testCaseTuple, String testSuiteDescription) {
    return template(testOracle.getDescription(), append(testCaseTuple, "@TESTSUITE", testSuiteDescription));
  }

  int getIndex();

  /**
   * Returns a string that describes this test oracle.
   *
   * Note that this method always returns raw form (a string before being templated).
   */
  String getDescription();

  String templateDescription(Tuple testCaseTuple, String testSuiteDescription);

  Function<Session, Action> createTestActionFactory(TestItem testItem, Tuple testCaseTuple);
}
