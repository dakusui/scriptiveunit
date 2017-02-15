package com.github.dakusui.scriptunit.drivers;

import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.annotations.Scriptable;
import com.github.dakusui.scriptunit.core.Utils;
import com.github.dakusui.scriptunit.model.func.Func;
import com.github.dakusui.scriptunit.model.Stage;

import java.math.BigDecimal;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class Arith {
  @ReflectivelyReferenced
  @SafeVarargs
  @Scriptable
  public final <T extends Stage> Func<T, BigDecimal> add(Func<T, Number>... numbers) {
    return (T input) -> calc(input, (BigDecimal v) -> v::add, numbers);
  }

  @ReflectivelyReferenced
  @SafeVarargs
  @Scriptable
  public final <T extends Stage> Func<T, BigDecimal> sub(Func<T, Number>... numbers) {
    return (T input) -> calc(input, (BigDecimal v) -> v::subtract, numbers);
  }

  @ReflectivelyReferenced
  @SafeVarargs
  @Scriptable
  public final <T extends Stage> Func<T, BigDecimal> mul(Func<T, Number>... numbers) {
    return (T input) -> calc(input, (BigDecimal v) -> v::multiply, numbers);
  }

  @ReflectivelyReferenced
  @SafeVarargs
  @Scriptable
  public final <T extends Stage> Func<T, BigDecimal> div(Func<T, Number>... numbers) {
    return (T input) -> calc(input, (BigDecimal v) -> v::divide, numbers);
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
