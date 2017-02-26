package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;

/**
 * An interface that represents a test result of a certain test case..
 *
 * @param <T>
 */
public interface TestIO<T> {
  /**
   * Returns a test case object.
   */
  Tuple getInput();

  /**
   * Returns output from SUT as an object of {@code T}.
   */
  T getOutput();

  static <T> TestIO<T> create(Tuple testCase, T output) {
    return new TestIO<T>() {
      @Override
      public Tuple getInput() {
        return testCase;
      }

      @Override
      public T getOutput() {
        return output;
      }
    };
  }
}
