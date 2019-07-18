package com.github.dakusui.scriptiveunit.model.desc;

import com.github.dakusui.scriptiveunit.core.JsonScript;
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

  JsonScript getConfig();

  Statement.Factory statementFactory();
}
