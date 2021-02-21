package com.github.dakusui.scriptiveunit.model.desc;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.value.ValueUtils;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.runners.RunningMode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public interface TestSuiteDescriptor {

  String getDescription();

  RunningMode getRunnerMode();

  ParameterSpaceDescriptor getParameterSpaceDescriptor();

  Map<String, List<Object>> getUserDefinedFormClauses();

  List<? extends TestOracle> getTestOracles();

  List<IndexedTestCase> getTestCases();

  Optional<Statement> setUpBeforeAll();

  Optional<Statement> setUp();

  Optional<Statement> tearDown();

  Optional<Statement> tearDownAfterAll();

  List<String> fixtureLevelParameterNames();

  default Tuple createFixtureTupleFrom(Tuple tuple) {
    Tuple.Builder b = new Tuple.Builder();
    for (String each : fixtureLevelParameterNames())
      b.put(each, tuple.get(each));
    return b.build();
  }

  Statement.Factory statementFactory();

  default Tuple createCommonFixture() {
    return Utils.createCommonFixture(getParameterSpaceDescriptor().getParameters());
  }

  enum Utils {
    ;

    /**
     * Extract all the factors whose values are not changed among in the test cases.
     *
     * @param parameters A list of all the parameters used  in the test suite
     * @return A tuple that holds unmodified factors and their values.
     */
    public static Tuple createCommonFixture(List<Parameter> parameters) {
      Tuple.Builder b = new Tuple.Builder();
      parameters.stream()
          .filter((Parameter in) -> in instanceof Parameter.Simple)
          .filter((Parameter in) -> in.getKnownValues().size() == 1)
          .forEach((Parameter in) -> b.put(in.getName(), in.getKnownValues().get(0)));
      return b.build();
    }

    public static List<String> fixtureLevelParameterNames(
        TestSuiteDescriptor testSuiteDescriptor) {
      List<String> singleLevelFactors = TestSuiteDescriptor.Utils.singleLevelFactors(testSuiteDescriptor);
      return Stream.concat(
          getInvolvedParameterNamesInSetUpAction(testSuiteDescriptor).stream(),
          singleLevelFactors.stream()
      ).distinct(
      ).collect(toList());
    }

    public static List<String> singleLevelFactors(TestSuiteDescriptor testSuiteDescriptor) {
      List<Parameter> parameters = testSuiteDescriptor.getParameterSpaceDescriptor().getParameters();
      return parameters.stream()
          .filter((Parameter each) -> each instanceof Parameter.Simple)
          .filter((Parameter each) -> each.getKnownValues().size() == 1)
          .map(Parameter::getName)
          .collect(toList());
    }

    private static List<String> getInvolvedParameterNamesInSetUpAction(TestSuiteDescriptor testSuiteDescriptor) {
      return testSuiteDescriptor.setUp()
          .map(ValueUtils::involvedParameters)
          .orElse(emptyList());
    }

  }
}
