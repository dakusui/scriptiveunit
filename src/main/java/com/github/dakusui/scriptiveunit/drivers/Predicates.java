package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.util.Objects;

import static com.github.dakusui.scriptiveunit.core.Utils.toBigDecimalIfPossible;
import static java.util.Objects.requireNonNull;

public class Predicates {
  @ReflectivelyReferenced
  @SafeVarargs
  @Scriptable
  @Doc({
      "Returns true if and only if all the given predicates return true.",
      "Predicates are evaluated sequentially in an order where they are given.",
      "When one of those predicate is evaluated false, the rest will not be evaluated" +
          "and false will be returned immediately."
  })
  public final Func<Boolean> and(
      @Doc("Predicates to be evaluated.") Func<Boolean>... predicates) {
    return (Stage input) -> {
      for (Func<Boolean> each : predicates) {
        if (!(requireNonNull(each.apply(input)))) {
          return false;
        }
      }
      return true;
    };
  }

  @ReflectivelyReferenced
  @SafeVarargs
  @Scriptable
  @Doc({
      "Returns true if any of the given predicates return true.",
      "Predicates are evaluated sequentially in an order where they are given.",
      "When one of those predicate is evaluated true, the rest will not be evaluated" +
          "and true will be returned immediately."
  })
  public final Func<Boolean> or(
      @Doc("Predicates to be evaluated.") Func<Boolean>... predicates) {
    return (Stage input) -> {
      for (Func<Boolean> each : predicates) {
        if (requireNonNull(each.apply(input))) {
          return true;
        }
      }
      return false;
    };
  }

  @ReflectivelyReferenced
  @Scriptable
  @Doc({
      "If the first argument is evaluated true, the second argument is evaluated" +
          " and the value will be returned. Otherwise, false will be returned without" +
          " evaluating the second argument.",
      "This function is useful to describe a constraint or a condition to ignore " +
          "a certain test oracle."
  })
  public Func<Boolean> ifthen(
      @Doc("A condition value") Func<Boolean> cond,
      @Doc("A condition value evaluated only when the first condition is met") Func<Boolean> then) {
    return (Stage input) -> requireNonNull(cond.apply(input)) ?
        then.apply(input) :
        true;
  }

  @ReflectivelyReferenced
  @Scriptable
  @Doc("Returns always true.")
  public Func<Boolean> always() {
    return input -> true;
  }

  @ReflectivelyReferenced
  @Scriptable
  @Doc(
      "Checks true if given values are equal to each other, false otherwise."
  )
  public <U> Func<Boolean> equals(
      @Doc("A value to be checked") Func<U> a,
      @Doc("A value to be checked") Func<U> b) {
    return input -> requireNonNull(Objects.equals(a.apply(input), b.apply(input)));
  }

  @ReflectivelyReferenced
  @Scriptable
  public Func<Boolean> not(Func<Boolean> predicate) {
    return input -> !requireNonNull(predicate.apply(input));
  }

  @ReflectivelyReferenced
  @Scriptable
  public <U> Func<Boolean> gt(Func<Comparable<U>> a, Func<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) > 0;
  }

  @ReflectivelyReferenced
  @Scriptable
  public <U> Func<Boolean> ge(Func<Comparable<U>> a, Func<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) >= 0;
  }

  @ReflectivelyReferenced
  @Scriptable
  public <U> Func<Boolean> lt(Func<Comparable<U>> a, Func<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) < 0;
  }

  @ReflectivelyReferenced
  @Scriptable
  public <U> Func<Boolean> le(Func<Comparable<U>> a, Func<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) <= 0;
  }

  @ReflectivelyReferenced
  @Scriptable
  public <U> Func<Boolean> eq(Func<Comparable<U>> a, Func<U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) == 0;
  }

  @ReflectivelyReferenced
  @Scriptable
  public <U> Func<Integer> compare(Func<Comparable<U>> a, Func<U> b) {
    return (Stage input) -> {
      Comparable valueOfA = requireNonNull((Comparable) toBigDecimalIfPossible(a.apply(input)));
      Object valueOfB = requireNonNull(toBigDecimalIfPossible(b.apply(input)));
      //noinspection unchecked
      return valueOfA.compareTo(valueOfB);
    };
  }
}
