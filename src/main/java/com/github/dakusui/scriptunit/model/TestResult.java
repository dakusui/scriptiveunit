package com.github.dakusui.scriptunit.model;

import com.github.dakusui.jcunit.framework.TestCase;

/**
 * An interface that represents a test result of a certain test case..
 *
 * @param <T>
 */
public interface TestResult<T> {
  /**
   * Returns a test case object.
   */
  TestCase getTestCase();

  /**
   * Returns output from SUT as an object of {@code T}.
   */
  T getOutput();

  static <T> TestResult<T> create(TestCase testCase, T output) {
    return new TestResult<T>() {
      @Override
      public TestCase getTestCase() {
        return testCase;
      }

      @Override
      public T getOutput() {
        return output;
      }
    };
  }
}
