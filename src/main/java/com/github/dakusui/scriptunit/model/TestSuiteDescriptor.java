package com.github.dakusui.scriptunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.scriptunit.GroupedTestItemRunner;
import com.github.dakusui.scriptunit.model.func.Func;

import java.util.List;

public interface TestSuiteDescriptor {
  String getDescription();

  GroupedTestItemRunner.Type getRunnerType();

  FactorSpaceDescriptor getFactorSpaceDescriptor();

  CoveringArrayEngineConfig getCoveringArrayEngineConfig();

  List<? extends TestOracle> getTestOracles();

  Func<Stage, Action> getSetUpActionFactory();

  Func<Stage, Action> getSetUpBeforeAllActionFactory();

  List<String> getInvolvedParameterNamesInSetUpAction();
}
