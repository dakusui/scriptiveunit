package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.function.Supplier;

public interface TestOracle {
  String getDescription();

  Supplier<Action> createTestActionSupplier(int itemId, Tuple testCase, TestSuiteDescriptor testSuiteDescriptor);
}
