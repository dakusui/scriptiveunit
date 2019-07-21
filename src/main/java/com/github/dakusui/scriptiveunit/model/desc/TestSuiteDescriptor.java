package com.github.dakusui.scriptiveunit.model.desc;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.runners.RunningMode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestSuiteDescriptor {
  String getDescription();

  RunningMode getRunnerMode();

  ParameterSpaceDescriptor getFactorSpaceDescriptor();

  Map<String, List<Object>> getUserDefinedFormClauses();

  List<? extends TestOracle> getTestOracles();

  List<IndexedTestCase> getTestCases();

  Optional<Statement> setUpBeforeAll();

  Optional<Statement> setUp();

  Optional<Statement> tearDown();

  Optional<Statement> tearDownAfterAll();

  List<String> getInvolvedParameterNamesInSetUpAction();

  Statement.Factory statementFactory();

  default Tuple createCommonFixture() {
    return Utils.createCommonFixture(getFactorSpaceDescriptor().getParameters());
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
  }
}
