package com.github.dakusui.scriptiveunit.exceptions;

import com.github.dakusui.faultsource.printable.Printable;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapIfNecessary;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public enum Requirements {
  ;

  public static <T> Predicate<? super T> isInstanceOf(Class<T> klass) {
    return Printable.predicate(
        () -> format("isInstanceOf[%s]", klass.getSimpleName()),
        v -> v != null && klass.isAssignableFrom(v.getClass()));
  }

  public static <V> V require(V v, Predicate<V> req, Function<String, ? extends RuntimeException> otherwise) {
    requireNonNull(req);
    if (req.test(v))
      return v;
    throw otherwise.apply(format("Value:<%s> did not satisfy precondition:<%s>",
        v,
        isPrintable(req) ?
            req.toString() :
            "(unknown)"));
  }

  private static boolean isPrintable(Object object) {
    return !getToStringMethod(requireNonNull(object).getClass()).equals(getToStringMethod(Object.class));
  }

  private static Method getToStringMethod(Class<?> aClass) {
    try {
      return aClass.getMethod("toString");
    } catch (NoSuchMethodException e) {
      throw wrapIfNecessary(e);
    }
  }
}
