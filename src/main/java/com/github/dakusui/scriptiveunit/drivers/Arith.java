package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormList;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.utils.CoreUtils;

import java.math.BigDecimal;
import java.util.function.Function;

import static java.math.MathContext.DECIMAL128;
import static java.util.Objects.requireNonNull;

public class Arith {
  @SuppressWarnings("unused")
  @Scriptable
  public final Form<BigDecimal> add(FormList<Number> numbers) {
    return (Stage input) -> calc(
        input,
        (BigDecimal v) -> (BigDecimal augend) -> v.add(augend, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Form<BigDecimal> sub(FormList<Number> numbers) {
    return (Stage input) -> calc(
        input,
        (BigDecimal v) -> (BigDecimal subtrahend) -> v.subtract(subtrahend, DECIMAL128), numbers
    );
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Form<BigDecimal> mul(FormList<Number> numbers) {
    return (Stage input) -> calc(input, (BigDecimal v) -> multiplicand -> v.multiply(multiplicand, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Form<BigDecimal> div(FormList<Number> numbers) {
    return (Stage input) -> calc(input, (BigDecimal v) -> divisor -> v.divide(divisor, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  private BigDecimal calc(Stage stage, Function<BigDecimal, Function<BigDecimal, BigDecimal>> op, FormList<Number> numbers) {
    BigDecimal ret = null;
    for (Object eachNumber : numbers) {
      BigDecimal each = CoreUtils.toBigDecimal((Number)
          ((Form) eachNumber).apply(stage));
      if (ret == null) {
        ret = requireNonNull(CoreUtils.toBigDecimal(each));
      } else {
        ret = op.apply(ret).apply(CoreUtils.toBigDecimal(each));
      }
    }
    return ret;
  }
}
