package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;

/**
 * An interface that represents a test result of a certain test case..
 *
 * @param <T>
 */
public interface TestResult<T> {
  /**
   * Returns a test case object.
   */
  Tuple getTestCase();

  /**
   * Returns output from SUT as an object of {@code T}.
   */
  T getOutput();

  static <T> TestResult<T> create(Tuple testCase, T output) {
    return new TestResult<T>() {
      @Override
      public Tuple getTestCase() {
        return testCase;
      }

      @Override
      public T getOutput() {
        return output;
      }
    };
  }
}
