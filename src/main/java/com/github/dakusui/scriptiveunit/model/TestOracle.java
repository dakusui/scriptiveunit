package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.Session;

import java.util.function.Function;

public interface TestOracle {
  int getIndex();

  String getDescription();

  Function<Session, Action> createTestActionFactory(TestItem testItem, Tuple testCaseTuple);
}
