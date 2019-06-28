package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import static com.github.dakusui.scriptiveunit.utils.StringUtils.template;
import static com.github.dakusui.scriptiveunit.utils.TupleUtils.append;

public enum TestItemUtils {
  ;

  public static String templateTestOracleDescription(Tuple testCaseTuple, String testSuiteDescription, String testOracleDescription) {
    return template(testOracleDescription, append(testCaseTuple, "@TESTSUITE", testSuiteDescription));
  }
}
