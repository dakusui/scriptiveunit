package com.github.dakusui.scriptunit.loaders;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.framework.TestCase;

import static java.lang.String.format;

public class IndexedTestCase extends TestCase {
  private final int index;

  private IndexedTestCase(int index, Category category, Tuple tuple) {
    super(category, tuple);
    this.index = index;
  }

  IndexedTestCase(int index, TestCase testCase) {
    this(index, testCase.getCategory(), testCase.getTuple());
  }

  public int getIndex() {
    return this.index;
  }

  @Override
  public String toString() {
    return format("%s:%s:%02d", this.getCategory(), this.getTuple(), this.getIndex());
  }
}
