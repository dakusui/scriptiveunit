package com.github.dakusui.scriptiveunit.testutils;

import org.hamcrest.Matcher;

/**
 * @param <I> Input to SUT
 * @param <S> Output from SUT
 */
public interface TestDef<I, T, S> {
  Matcher<S> getOracle(T testObject);
  I getTestInput();
}
