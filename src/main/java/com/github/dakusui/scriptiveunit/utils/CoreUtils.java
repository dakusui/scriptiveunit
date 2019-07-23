package com.github.dakusui.scriptiveunit.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.scriptiveunit.core.Exceptions.SCRIPTIVEUNIT;
import static java.math.MathContext.DECIMAL128;

public enum CoreUtils {
  ;

  public static <V> Stream<V> iterableToStream(Iterable<V> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false);
  }

  public static BigDecimal toBigDecimal(Number number) {
    if (number instanceof BigDecimal)
      return (BigDecimal) number;
    return new BigDecimal(number.toString(), DECIMAL128);
  }

  public static Object toBigDecimalIfPossible(Object object) {
    if (object instanceof Number) {
      return toBigDecimal((Number) object);
    }
    return object;
  }

  public static <T> T car(T[] arr) {
    return SCRIPTIVEUNIT.requireValue(v -> v.length > 0, SCRIPTIVEUNIT.requireNonNull(arr))[0];
  }

  public static <T> T[] cdr(T[] arr) {
    return Arrays.copyOfRange(
        SCRIPTIVEUNIT.requireValue(v -> v.length > 0, SCRIPTIVEUNIT.requireNonNull(arr)),
        1,
        arr.length
    );
  }

  public static boolean isAtom(Object object) {
    return !(object instanceof List) || ((List) object).isEmpty();
  }

  public static Object car(List<Object> raw) {
    return raw.get(0);
  }

  public static List<Object> cdr(List<Object> raw) {
    return raw.subList(1, raw.size());
  }

  public static <T> Collector<T, List<T>, T> singletonCollector() {
    return singletonCollector(IllegalStateException::new, IllegalStateException::new);
  }

  public static <T> Collector<T, List<T>, T> singletonCollector(Supplier<RuntimeException> onNotFound, Supplier<RuntimeException> onMultipleElements) {
    return Collector.of(
        ArrayList::new,
        (ts, t) -> {
          if (ts.isEmpty()) {
            ts.add(t);
            return;
          }
          throw onMultipleElements.get();
        },
        (left, right) -> {
          if (left.size() == 1 && right.isEmpty() || left.isEmpty() && right.size() == 1) {
            left.addAll(right);
            return left;
          }
          throw onMultipleElements.get();
        },
        list -> {
          if (list.isEmpty())
            throw onNotFound.get();
          if (list.size() > 1)
            throw onMultipleElements.get();
          return list.get(0);
        }
    );
  }
}
