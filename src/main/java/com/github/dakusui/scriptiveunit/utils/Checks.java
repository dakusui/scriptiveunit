package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitUnclassifiedException;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public enum Checks {
  ;

  public static <E extends ScriptiveUnitException> void check(boolean cond, Supplier<E> thrower) {
    if (!cond)
      throw thrower.get();
  }

  public static <E extends ScriptiveUnitException, V> V check(V target, Predicate<? super V> predicate,
                                                              Supplier<? extends E> thrower) {
    if (!requireNonNull(predicate).test(target))
      throw thrower.get();
    return target;
  }

  public static <V> V check(V target, Predicate<? super V> predicate, String fmt, Object... args) {
    if (!requireNonNull(predicate).test(target))
      throw new ScriptiveUnitUnclassifiedException(String.format(fmt, args));
    return target;
  }
}
