package com.github.dakusui.scriptunit.testutils;

import org.hamcrest.Matcher;

/**
 * @param <I> Input to SUT
 * @param <S> Output from SUT
 */
public interface TestDef<I, S> {
  I getTestInput();

  Matcher<S> getOracle();
}
