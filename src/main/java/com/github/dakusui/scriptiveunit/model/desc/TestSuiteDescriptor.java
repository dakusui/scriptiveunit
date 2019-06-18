package com.github.dakusui.scriptiveunit.model.desc;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.List;
import java.util.Map;

public interface TestSuiteDescriptor {
  String getDescription();

  ScriptiveUnit.Mode getRunnerMode();

  ParameterSpaceDescriptor getFactorSpaceDescriptor();

  Map<String, List<Object>> getUserDefinedFormClauses();

  List<? extends TestOracle> getTestOracles();

  List<IndexedTestCase> getTestCases();

  Form<Action> getSetUpBeforeAllActionFactory();

  Form<Action> getSetUpActionFactory();

  Form<Action> getTearDownActionFactory();

  Form<Action> getTearDownAfterAllActionFactory();

  List<String> getInvolvedParameterNamesInSetUpAction();

  Config getConfig();

  Statement.Factory statementFactory();

}
