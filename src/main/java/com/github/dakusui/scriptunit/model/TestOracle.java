package com.github.dakusui.scriptunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.framework.TestCase;

public interface TestOracle {
  Action createTestAction(String testSuiteDescription, int groupId, int itemId, TestCase testCase, Func<Stage, Action> setUpFactory);
}
