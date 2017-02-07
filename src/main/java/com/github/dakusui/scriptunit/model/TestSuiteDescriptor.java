package com.github.dakusui.scriptunit.model;

import com.github.dakusui.scriptunit.ScriptRunner;

import java.util.List;

public interface TestSuiteDescriptor {
  String getDescription();

  FactorSpaceDescriptor getFactorSpaceDescriptor();

  CoveringArrayEngineConfig getCoveringArrayEngineConfig();

  List<? extends TestOracle> getTestOracles();

  ScriptRunner.Type getRunnerType();
}
