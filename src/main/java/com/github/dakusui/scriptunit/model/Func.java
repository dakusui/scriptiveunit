package com.github.dakusui.scriptunit.model;

import java.io.IOException;
import java.util.Formattable;
import java.util.Formatter;

import static com.github.dakusui.scriptunit.exceptions.ScriptUnitException.wrap;

@FunctionalInterface
public interface Func<I, O> extends
    java.util.function.Function<I, O>,
    com.google.common.base.Function<I, O>,
    Formattable {
  @Override
  O apply(I input);

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    try {
      formatter.out().append("(unprintable)");
    } catch (IOException e) {
      throw wrap(e);
    }
  }

  interface Memoized<I, O> extends Func<I, O> {
  }

  /**
   * An interface that represents a constant. The {@code apply} method of an
   * implementation of this class must return the same value always regardless
   * of its arguments value, even if a {@code null} is given.
   *
   * @param <O> Type of output constant.
   */
  interface Const<O> extends Func<Object, O> {
  }
}
