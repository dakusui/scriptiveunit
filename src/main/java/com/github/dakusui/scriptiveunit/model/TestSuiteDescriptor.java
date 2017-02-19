package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.scriptiveunit.GroupedTestItemRunner;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.util.List;

public interface TestSuiteDescriptor {
  Object getDriverObject();

  String getDescription();

  GroupedTestItemRunner.Type getRunnerType();

  FactorSpaceDescriptor getFactorSpaceDescriptor();

  CoveringArrayEngineConfig getCoveringArrayEngineConfig();

  List<? extends TestOracle> getTestOracles();

  List<IndexedTestCase> getTestCases();

  Func<Stage, Action> getSetUpActionFactory();

  Func<Stage, Action> getSetUpBeforeAllActionFactory();

  List<String> getInvolvedParameterNamesInSetUpAction();
}
