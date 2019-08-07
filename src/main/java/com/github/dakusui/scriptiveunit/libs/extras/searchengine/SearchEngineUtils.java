package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.stage.Stage;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public enum SearchEngineUtils {
  ;

  public static <F> Value<F> toValue(F entry) {
    return input -> entry;
  }

  public static <F> Value<F> toValue(String name, F entry) {
    return Value.Named.create(name, stage -> entry);
  }

  public static <E> Stage wrapValueAsArgumentInStage(Stage i, Value<E> value) {
    return Stage.Factory.createWrappedStage(i, value);
  }

  @SafeVarargs
  public static <E> Stage wrapValuesAsArgumentsInStage(Stage i, Value<E>... values) {
    return Stage.Factory.createWrappedStage(i, values);
  }

  public static <T> Predicate<T> printablePredicate(String name, Predicate<T> predicate) {
    return new Predicate<T>() {
      @Override
      public boolean test(T t) {
        return predicate.test(t);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <T, U> BiPredicate<T, U> printableBiPredicate(String name, BiPredicate<T, U> predicate) {
    return new BiPredicate<T, U>() {
      @Override
      public boolean test(T t, U u) {
        return predicate.test(t, u);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <T> ToDoubleFunction<T> printableToDoubleFunction(String name, ToDoubleFunction<T> func) {
    return new ToDoubleFunction<T>() {
      @Override
      public double applyAsDouble(T value) {
        return func.applyAsDouble(value);
      }

      @Override
      public String toString() {
        return Objects.toString(name);
      }
    };
  }

  public static <U> U evaluateValueWithoutListening(Stage stage, Value<U> value) {
    return evaluateValueWithoutListening(stage, value, Value::apply);
  }

  public static <U> U evaluateValueWithoutListening(Stage stage, Value<U> value, BiFunction<Value<U>, Stage, U> applier) {
    return applier.apply(value, stage);
  }
}
