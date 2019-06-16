package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.form.Form;

import java.math.BigDecimal;
import java.util.function.Function;

import static java.math.MathContext.DECIMAL128;
import static java.util.Objects.requireNonNull;

public class Arith {
  @SuppressWarnings("unused")
  @SafeVarargs
  @Scriptable
  public final Form<BigDecimal> add(Form<Number>... numbers) {
    return (Stage input) -> calc(
        input,
        (BigDecimal v) -> (BigDecimal augend) -> v.add(augend, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  @SafeVarargs
  @Scriptable
  public final Form<BigDecimal> sub(Form<Number>... numbers) {
    return (Stage input) -> calc(
        input,
        (BigDecimal v) -> (BigDecimal subtrahend) -> v.subtract(subtrahend, DECIMAL128), numbers
    );
  }

  @SuppressWarnings("unused")
  @SafeVarargs
  @Scriptable
  public final Form<BigDecimal> mul(Form<Number>... numbers) {
    return (Stage input) -> calc(input, (BigDecimal v) -> multiplicand -> v.multiply(multiplicand, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  @SafeVarargs
  @Scriptable
  public final Form<BigDecimal> div(Form<Number>... numbers) {
    return (Stage input) -> calc(input, (BigDecimal v) -> divisor -> v.divide(divisor, DECIMAL128), numbers);
  }

  @SuppressWarnings("unused")
  @SafeVarargs
  private final BigDecimal calc(Stage stage, Function<BigDecimal, Function<BigDecimal, BigDecimal>> op, Form<Number>... numbers) {
    BigDecimal ret = null;
    for (Form<Number> eachNumber : numbers) {
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
