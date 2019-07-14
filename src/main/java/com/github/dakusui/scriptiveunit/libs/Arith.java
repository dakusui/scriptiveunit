package com.github.dakusui.scriptiveunit.libs;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.utils.CoreUtils;

import java.math.BigDecimal;
import java.util.function.Function;

import static java.math.MathContext.DECIMAL128;
import static java.util.Objects.requireNonNull;

public class Arith {
  @SuppressWarnings("unused")
  @Scriptable
  public final Value<BigDecimal> add(ValueList<Number> numbers) {
    return (Stage input) -> calc(
        input,
        (BigDecimal v) -> (BigDecimal augend) -> v.add(augend, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Value<BigDecimal> sub(ValueList<Number> numbers) {
    return (Stage input) -> calc(
        input,
        (BigDecimal v) -> (BigDecimal subtrahend) -> v.subtract(subtrahend, DECIMAL128), numbers
    );
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Value<BigDecimal> mul(ValueList<Number> numbers) {
    return (Stage input) -> calc(input, (BigDecimal v) -> multiplicand -> v.multiply(multiplicand, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Value<BigDecimal> div(ValueList<Number> numbers) {
    return (Stage input) -> calc(input, (BigDecimal v) -> divisor -> v.divide(divisor, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  private BigDecimal calc(Stage stage, Function<BigDecimal, Function<BigDecimal, BigDecimal>> op, ValueList<Number> numbers) {
    BigDecimal ret = null;
    for (Object eachNumber : numbers) {
      BigDecimal each = CoreUtils.toBigDecimal((Number)
          ((Value) eachNumber).apply(stage));
      if (ret == null) {
        ret = requireNonNull(CoreUtils.toBigDecimal(each));
      } else {
        ret = op.apply(ret).apply(CoreUtils.toBigDecimal(each));
      }
    }
    return ret;
  }
}
