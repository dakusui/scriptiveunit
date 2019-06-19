package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;

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
      throw new ScriptiveUnitException(String.format(fmt, args));
    return target;
  }

  // safe because both Long.class and long.class are of type Class<Long>
  @SuppressWarnings("unchecked")
  public static <T> Class<T> wrap(Class<T> c) {
    return c.isPrimitive() ? (Class<T>) CoreUtils.PRIMITIVES_TO_WRAPPERS.get(c) : c;
  }

  public static Object[] check(Object[] args, Predicate<Object[]> isCond, Object o) {
    return new Object[0];
  }
}
