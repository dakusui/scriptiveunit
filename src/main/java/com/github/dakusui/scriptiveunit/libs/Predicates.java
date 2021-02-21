package com.github.dakusui.scriptiveunit.libs;

import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;
import com.github.dakusui.scriptiveunit.model.stage.Stage;

import java.util.Objects;

import static com.github.dakusui.scriptiveunit.utils.CoreUtils.toBigDecimalIfPossible;
import static java.util.Objects.requireNonNull;

public class Predicates {
  @Scriptable
  @Doc({
      "Returns true if and only if all the given predicates return true.",
      "Predicates are evaluated sequentially in an order where they are given.",
      "Even when one of those predicate is evaluated false, the rest will still be evaluated.",
      "In case no predicate is given, this returns true"
  })
  public final Value<Boolean> allOf(
      @Doc("Predicates to be evaluated.") ValueList<Boolean> predicates) {
    return (Stage input) -> {
      boolean ret = true;
      for (Value<Boolean> each : predicates) {
        if (!(requireNonNull(each.apply(input)))) {
          ret = false;
        }
      }
      return ret;
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  @Doc({
      "Returns true if and only if all the given predicates return true.",
      "Predicates are evaluated sequentially in an order where they are given.",
      "When one of those predicate is evaluated false, the rest will not be evaluated" +
          "and false will be returned immediately."
  })
  public final Value<Boolean> and(
      @Doc("Predicates to be evaluated.") ValueList<Boolean> predicates) {
    return (Stage input) -> {
      for (Value<Boolean> each : predicates) {
        if (!(requireNonNull(each.apply(input)))) {
          return false;
        }
      }
      return true;
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  @Doc({
      "Returns true if any of the given predicates return true.",
      "Predicates are evaluated sequentially in an order where they are given.",
      "Even when one of those predicate is evaluated true, the rest will still be evaluated.",
      "In case no predicate is given, this returns false"
  })
  public final Value<Boolean> anyOf(
      @Doc("Predicates to be evaluated.") ValueList<Boolean> predicates) {
    return (Stage input) -> {
      boolean ret = false;
      for (Value<Boolean> each : predicates) {
        if (!(requireNonNull(each.apply(input)))) {
          ret = true;
        }
      }
      return ret;
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  @Doc({
      "Returns true if any of the given predicates return true.",
      "Predicates are evaluated sequentially in an order where they are given.",
      "When one of those predicate is evaluated true, the rest will not be evaluated" +
          " and true will be returned immediately."
  })
  public final Value<Boolean> or(
      @Doc("Predicates to be evaluated.") ValueList<Boolean> predicates) {
    return (Stage input) -> {
      for (Value<Boolean> each : predicates) {
        if (requireNonNull(each.apply(input))) {
          return true;
        }
      }
      return false;
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  @Doc({
      "If the first argument is evaluated true, the second argument is evaluated" +
          " and the value will be returned. Otherwise, false will be returned without" +
          " evaluating the second argument.",
      "This function is useful to describe a constraint or a condition to ignore" +
          " a certain test oracle."
  })
  public Value<Boolean> ifthen(
      @Doc("A condition value") Value<Boolean> cond,
      @Doc("A condition value evaluated only when the first condition is met") Value<Boolean> then) {
    return (Stage input) -> requireNonNull(cond.apply(input)) ?
        then.apply(input) :
        true;
  }

  @SuppressWarnings("unused")
  @Scriptable
  @Doc("Returns always true.")
  public Value<Boolean> always() {
    return input -> true;
  }

  @SuppressWarnings("unused")
  @Scriptable
  @Doc(
      "Checks true if given values are equal to each other, false otherwise."
  )
  public <U> Value<Boolean> equals(
      @Doc("A value to be checked") Value<U> a,
      @Doc("A value to be checked") Value<U> b) {
    return input -> Objects.equals(a.apply(input), b.apply(input));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Value<Boolean> not(Value<Boolean> predicate) {
    return input -> !requireNonNull(predicate.apply(input));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <U> Value<Boolean> gt(Value<Comparable<U>> a, Value<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) > 0;
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <U> Value<Boolean> ge(Value<Comparable<U>> a, Value<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) >= 0;
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <U> Value<Boolean> lt(Value<Comparable<U>> a, Value<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) < 0;
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <U> Value<Boolean> le(Value<Comparable<U>> a, Value<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) <= 0;
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <U> Value<Boolean> eq(Value<Comparable<U>> a, Value<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) == 0;
  }

  @SuppressWarnings({ "unused", "unchecked" })
  @Scriptable
  public <U> Value<Integer> compare(Value<Comparable<U>> a, Value<U> b) {
    return (Stage input) -> {
      Comparable valueOfA = requireNonNull((Comparable) toBigDecimalIfPossible(a.apply(input)));
      Object valueOfB = requireNonNull(toBigDecimalIfPossible(b.apply(input)));
      return valueOfA.compareTo(valueOfB);
    };
  }
}
