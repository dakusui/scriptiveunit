package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;

import static com.github.dakusui.scriptiveunit.utils.StringUtils.template;
import static com.github.dakusui.scriptiveunit.utils.TupleUtils.append;
import static com.github.dakusui.scriptiveunit.utils.TupleUtils.filterSimpleSingleLevelParametersOut;

public enum TestItemUtils {
  ;

  public static String templateTestOracleDescription(Tuple testCaseTuple, String testSuiteDescription, String testOracleDescription) {
    return template(testOracleDescription, append(testCaseTuple, "@TESTSUITE", testSuiteDescription));
  }

  public static String formatTestName(Tuple tuple, TestSuiteDescriptor testSuiteDescriptor, String testOracleDescription) {
    Tuple filteredTuple = filterSimpleSingleLevelParametersOut(
        tuple,
        testSuiteDescriptor.getParameterSpaceDescriptor().getParameters()
    );
    return templateTestOracleDescription(
        tuple,
        testSuiteDescriptor.getDescription(),
        "Verify <" + testOracleDescription + ">" +
            (filteredTuple.isEmpty() ?
                "" :
                " with: " + filteredTuple)
    );
  }
}
