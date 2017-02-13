package com.github.dakusui.scriptunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.scriptunit.ScriptRunner;

import java.util.List;

public interface TestSuiteDescriptor {
  String getDescription();

  ScriptRunner.Type getRunnerType();

  FactorSpaceDescriptor getFactorSpaceDescriptor();

  CoveringArrayEngineConfig getCoveringArrayEngineConfig();

  List<? extends TestOracle> getTestOracles();

  Func<Stage, Action> getSetUpActionFactory();

  Func<Stage, Action> getSetUpBeforeAllActionFactory();

  List<String> getInvolvedParameterNamesInSetUpAction();
}
