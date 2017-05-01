package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.testsuite.TestCase;

import java.util.List;

public class IndexedTestCase implements TestCase {
  private final int      index;
  private final TestCase testCase;

  public IndexedTestCase(int index, TestCase testCase) {
    this.index = index;
    this.testCase = testCase;
  }

  public int getIndex() {
    return this.index;
  }

  @Override
  public Tuple get() {
    return testCase.get();
  }

  @Override
  public Category getCategory() {
    return testCase.getCategory();
  }

  @Override
  public List<Constraint> violatedConstraints() {
    return testCase.violatedConstraints();
  }
}
