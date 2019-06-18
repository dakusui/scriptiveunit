package com.github.dakusui.scriptiveunit.utils;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.MathContext.DECIMAL128;

public enum CoreUtils {
  ;
  public static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new ImmutableMap.Builder<Class<?>, Class<?>>()
      .put(boolean.class, Boolean.class).put(byte.class, Byte.class).put(char.class, Character.class)
      .put(double.class, Double.class).put(float.class, Float.class).put(int.class, Integer.class)
      .put(long.class, Long.class).put(short.class, Short.class).put(void.class, Void.class).build();

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
}
