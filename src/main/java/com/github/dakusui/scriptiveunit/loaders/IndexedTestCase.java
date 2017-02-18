package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.framework.TestCase;

public class IndexedTestCase extends TestCase {
  private final int index;

  IndexedTestCase(int index, TestCase testCase) {
    this(index, testCase.getCategory(), testCase.getTuple());
  }

  public int getIndex() {
    return this.index;
  }

  private IndexedTestCase(int index, Category category, Tuple tuple) {
    super(category, tuple);
    this.index = index;
  }
}
