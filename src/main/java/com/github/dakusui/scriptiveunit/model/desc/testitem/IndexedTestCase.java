package com.github.dakusui.scriptiveunit.model.desc.testitem;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.testsuite.TestCase;

import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

public class IndexedTestCase implements TestCase {
  private final int              index;
  private final Tuple            tuple;
  private final Category         category;
  private final List<Constraint> violatedConstraints;

  public IndexedTestCase(int index, TestCase testCase) {
    this.index = index;
    this.tuple = testCase.getTestInput();
    this.category = testCase.getCategory();
    this.violatedConstraints = testCase.violatedConstraints();
  }

  public IndexedTestCase(int index, Tuple tuple, Category category) {
    this.index = index;
    this.tuple = tuple;
    this.category = category;
    this.violatedConstraints = emptyList();
  }

  public int getIndex() {
    return this.index;
  }

  @Override
  public Tuple getTestInput() {
    return this.tuple;
  }

  @Override
  public Category getCategory() {
    return this.category;
  }

  @Override
  public List<Constraint> violatedConstraints() {
    return this.violatedConstraints;
  }

  @Override
  public String toString() {
    return format("testCase[%s]:{category=%s; values=%s; violatedConstraints=%s}",
        this.index,
        this.category,
        this.tuple,
        this.violatedConstraints);
  }

}
