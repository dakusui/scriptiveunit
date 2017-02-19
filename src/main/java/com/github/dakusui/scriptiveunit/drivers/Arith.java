package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.math.BigDecimal;
import java.util.function.Function;

import static java.math.MathContext.DECIMAL128;
import static java.util.Objects.requireNonNull;

public class Arith {
  @ReflectivelyReferenced
  @SafeVarargs
  @Scriptable
  public final <T extends Stage> Func<T, BigDecimal> add(Func<T, Number>... numbers) {
    return (T input) -> calc(input, (BigDecimal v) -> augend -> v.add(augend, DECIMAL128), numbers);
  }

  @ReflectivelyReferenced
  @SafeVarargs
  @Scriptable
  public final <T extends Stage> Func<T, BigDecimal> sub(Func<T, Number>... numbers) {
    return (T input) -> calc(input, (BigDecimal v) -> subtrahend -> v.subtract(subtrahend, DECIMAL128), numbers);
  }

  @ReflectivelyReferenced
  @SafeVarargs
  @Scriptable
  public final <T extends Stage> Func<T, BigDecimal> mul(Func<T, Number>... numbers) {
    return (T input) -> calc(input, (BigDecimal v) -> multiplicand -> v.multiply(multiplicand, DECIMAL128), numbers);
  }

  @ReflectivelyReferenced
  @SafeVarargs
  @Scriptable
  public final <T extends Stage> Func<T, BigDecimal> div(Func<T, Number>... numbers) {
    return (T input) -> calc(input, (BigDecimal v) -> divisor -> v.divide(divisor, DECIMAL128), numbers);
  }

  @ReflectivelyReferenced
  @SafeVarargs
  private final <T extends Stage> BigDecimal calc(T stage, Function<BigDecimal, Function<BigDecimal, BigDecimal>> op, Func<T, Number>... numbers) {
    BigDecimal ret = null;
    for (Func<T, Number> eachNumber : numbers) {
      BigDecimal each = Utils.toBigDecimal(eachNumber.apply(stage));
      if (ret == null) {
        ret = requireNonNull(Utils.toBigDecimal(each));
      } else {
        ret = op.apply(ret).apply(Utils.toBigDecimal(each));
      }
    }
    return ret;
  }
}