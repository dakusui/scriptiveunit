package com.github.dakusui.scriptunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.List;
import java.util.function.Supplier;

public interface TestOracle {
  String getDescription();

  Supplier<Action> createTestActionSupplier(List<Factor> factors, int itemId, String testSuiteDescription, Tuple testCase);
}
