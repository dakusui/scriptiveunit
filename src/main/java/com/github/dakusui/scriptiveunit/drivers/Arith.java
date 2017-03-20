package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.math.BigDecimal;
import java.util.function.Function;

import static java.math.MathContext.DECIMAL128;
import static java.util.Objects.requireNonNull;

public class Arith {
  @SuppressWarnings("unused")
  @SafeVarargs
  @Scriptable
  public final Func<BigDecimal> add(Func<Number>... numbers) {
    return (Stage input) -> calc(input, (BigDecimal v) -> augend -> v.add(augend, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  @SafeVarargs
  @Scriptable
  public final Func<BigDecimal> sub(Func<Number>... numbers) {
    return (Stage input) -> calc(input, (BigDecimal v) -> subtrahend -> v.subtract(subtrahend, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  @SafeVarargs
  @Scriptable
  public final Func<BigDecimal> mul(Func<Number>... numbers) {
    return (Stage input) -> calc(input, (BigDecimal v) -> multiplicand -> v.multiply(multiplicand, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  @SafeVarargs
  @Scriptable
  public final Func<BigDecimal> div(Func<Number>... numbers) {
    return (Stage input) -> calc(input, (BigDecimal v) -> divisor -> v.divide(divisor, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  @SafeVarargs
  private final BigDecimal calc(Stage stage, Function<BigDecimal, Function<BigDecimal, BigDecimal>> op, Func<Number>... numbers) {
    BigDecimal ret = null;
    for (Func<Number> eachNumber : numbers) {
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
