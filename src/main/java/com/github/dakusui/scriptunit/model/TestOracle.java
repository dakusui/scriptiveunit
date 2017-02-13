package com.github.dakusui.scriptunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;

public interface TestOracle {
  String getDescription();
  Action createTestAction(int itemId, String testSuiteDescription, Tuple testCase);
}
