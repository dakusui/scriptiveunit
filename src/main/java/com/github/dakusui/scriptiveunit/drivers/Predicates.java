package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Form;

import java.util.Objects;

import static com.github.dakusui.scriptiveunit.core.Utils.toBigDecimalIfPossible;
import static java.util.Objects.requireNonNull;

public class Predicates {
  @SuppressWarnings("unused")
  @SafeVarargs
  @Scriptable
  @Doc({
      "Returns true if and only if all the given predicates return true.",
      "Predicates are evaluated sequentially in an order where they are given.",
      "When one of those predicate is evaluated false, the rest will not be evaluated" +
          "and false will be returned immediately."
  })
  public final Form<Boolean> and(
      @Doc("Predicates to be evaluated.") Form<Boolean>... predicates) {
    return (Stage input) -> {
      for (Form<Boolean> each : predicates) {
        if (!(requireNonNull(each.apply(input)))) {
          return false;
        }
      }
      return true;
    };
  }

  @SuppressWarnings("unused")
  @SafeVarargs
  @Scriptable
  @Doc({
      "Returns true if any of the given predicates return true.",
      "Predicates are evaluated sequentially in an order where they are given.",
      "When one of those predicate is evaluated true, the rest will not be evaluated" +
          "and true will be returned immediately."
  })
  public final Form<Boolean> or(
      @Doc("Predicates to be evaluated.") Form<Boolean>... predicates) {
    return (Stage input) -> {
      for (Form<Boolean> each : predicates) {
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
      "This function is useful to describe a constraint or a condition to ignore " +
          "a certain test oracle."
  })
  public Form<Boolean> ifthen(
      @Doc("A condition value") Form<Boolean> cond,
      @Doc("A condition value evaluated only when the first condition is met") Form<Boolean> then) {
    return (Stage input) -> requireNonNull(cond.apply(input)) ?
        then.apply(input) :
        true;
  }

  @SuppressWarnings("unused")
  @Scriptable
  @Doc("Returns always true.")
  public Form<Boolean> always() {
    return input -> true;
  }

  @SuppressWarnings("unused")
  @Scriptable
  @Doc(
      "Checks true if given values are equal to each other, false otherwise."
  )
  public <U> Form<Boolean> equals(
      @Doc("A value to be checked") Form<U> a,
      @Doc("A value to be checked") Form<U> b) {
    return input -> requireNonNull(Objects.equals(a.apply(input), b.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Boolean> not(Form<Boolean> predicate) {
    return input -> !requireNonNull(predicate.apply(input));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <U> Form<Boolean> gt(Form<Comparable<U>> a, Form<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) > 0;
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <U> Form<Boolean> ge(Form<Comparable<U>> a, Form<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) >= 0;
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <U> Form<Boolean> lt(Form<Comparable<U>> a, Form<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) < 0;
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <U> Form<Boolean> le(Form<Comparable<U>> a, Form<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) <= 0;
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <U> Form<Boolean> eq(Form<Comparable<U>> a, Form<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) == 0;
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <U> Form<Integer> compare(Form<Comparable<U>> a, Form<U> b) {
    return (Stage input) -> {
      Comparable valueOfA = requireNonNull((Comparable) toBigDecimalIfPossible(a.apply(input)));
      Object valueOfB = requireNonNull(toBigDecimalIfPossible(b.apply(input)));
      //noinspection unchecked
      return valueOfA.compareTo(valueOfB);
    };
  }
}
