package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.scriptiveunit.GroupedTestItemRunner;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.util.List;
import java.util.Map;

public interface TestSuiteDescriptor {
  Object getDriverObject();

  String getDescription();

  GroupedTestItemRunner.Type getRunnerType();

  FactorSpaceDescriptor getFactorSpaceDescriptor();

  CoveringArrayEngineConfig getCoveringArrayEngineConfig();

  Map<String, List<Object>> getUserDefinedFormClauses();

  List<? extends TestOracle> getTestOracles();

  List<IndexedTestCase> getTestCases();

  Func<Action> getSetUpActionFactory();

  Func<Action> getSetUpBeforeAllActionFactory();

  List<String> getInvolvedParameterNamesInSetUpAction();
}
