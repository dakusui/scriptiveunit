package com.github.dakusui.scriptunit.testutils.drivers;

import com.github.dakusui.scriptunit.annotations.Doc;
import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.annotations.Scriptable;
import com.github.dakusui.scriptunit.model.func.Func;
import com.github.dakusui.scriptunit.model.Stage;

import java.util.Objects;

import static com.github.dakusui.scriptunit.core.Utils.toBigDecimalIfPossible;
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
  public final <T extends Stage> Func<T, Boolean> and(
      @Doc("Predicates to be evaluated.") Func<T, Boolean>... predicates) {
    return (T input) -> {
      for (Func<T, Boolean> each : predicates) {
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
  public final <T extends Stage> Func<T, Boolean> or(
      @Doc("Predicates to be evaluated.") Func<T, Boolean>... predicates) {
    return (T input) -> {
      for (Func<T, Boolean> each : predicates) {
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
  public <T extends Stage> Func<T, Boolean> ifthen(
      @Doc("A condition value") Func<T, Boolean> cond,
      @Doc("A condition value evaluated only when the first condition is met") Func<T, Boolean> then) {
    return (T input) -> requireNonNull(cond.apply(input)) ?
        then.apply(input) :
        true;
  }

  @ReflectivelyReferenced
  @Scriptable
  @Doc("Returns always true.")
  public <T extends Stage> Func<T, Boolean> always() {
    return input -> true;
  }

  @ReflectivelyReferenced
  @Scriptable
  @Doc(
      "Checks true if given values are equal to each other, false otherwise."
  )
  public <T extends Stage, U> Func<T, Boolean> equals(
      @Doc("A value to be checked") Func<T, U> a,
      @Doc("A value to be checked") Func<T, U> b) {
    return input -> requireNonNull(Objects.equals(a.apply(input), b.apply(input)));
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage> Func<T, Boolean> not(Func<T, Boolean> predicate) {
    return input -> !requireNonNull(predicate.apply(input));
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage, U> Func<T, Boolean> gt(Func<T, Comparable<U>> a, Func<T, U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) > 0;
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage, U> Func<T, Boolean> ge(Func<T, Comparable<U>> a, Func<T, U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) >= 0;
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage, U> Func<T, Boolean> lt(Func<T, Comparable<U>> a, Func<T, U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) < 0;
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage, U> Func<T, Boolean> le(Func<T, Comparable<U>> a, Func<T, U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) <= 0;
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage, U> Func<T, Boolean> eq(Func<T, Comparable<U>> a, Func<T, U> b) {
    return input -> requireNonNull(requireNonNull(compare(a, b)).apply(input)) == 0;
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage, U> Func<T, Integer> compare(Func<T, Comparable<U>> a, Func<T, U> b) {
    return (T input) -> {
      Comparable valueOfA = requireNonNull((Comparable) toBigDecimalIfPossible(a.apply(input)));
      Object valueOfB = requireNonNull(toBigDecimalIfPossible(b.apply(input)));
      //noinspection unchecked
      return valueOfA.compareTo(valueOfB);
    };
  }
}
