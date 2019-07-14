package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Value;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.google.common.collect.Iterables;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

@SuppressWarnings("unused")
public class Collections {
  @SuppressWarnings("unused")
  @Scriptable
  public <E> Value<Integer> size(Value<Iterable<? extends E>> iterable) {
    return (Stage input) -> Iterables.size(requireNonNull(iterable.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <E> Value<Integer> concat(Value<Iterable<? extends E>> iterable) {
    return (Stage input) -> Iterables.size(requireNonNull(iterable.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <E> Value<Iterable<? extends E>> compatFilter(Value<Iterable<? extends E>> iterable, Value<Function<E, Boolean>> predicate) {
    return (Stage i) -> (Iterable<? extends E>) stream(
        requireNonNull(iterable.apply(i)).spliterator(),
        false
    ).filter(
        input -> requireNonNull(requireNonNull(predicate.apply(i)).apply(input))
    ).collect(
        Collectors.<E>toList()
    );
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <E> Value<Iterable<? extends E>> filter(Value<Iterable<? extends E>> iterable, Value<Value<Boolean>> predicate) {
    return (Stage i) -> (Iterable<? extends E>) stream(
        requireNonNull(iterable.apply(i)).spliterator(),
        false
    ).filter(
        (E entry) -> predicate.apply(i).apply(wrapValueAsArgumentInStage(i, toFunc(entry)))
    ).collect(
        Collectors.<E>toList()
    );
  }

  private static <F> Value<F> toFunc(F entry) {
    return input -> entry;
  }

  public static <E> Stage wrapValueAsArgumentInStage(Stage i, Value<E> value) {
    return Stage.Factory.createWrappedStage(i, value);
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <E> Value<Function<E, Boolean>> containedBy(Value<Iterable<E>> iterable) {
    return (Stage input) -> {
      Iterable<E> collection = requireNonNull(iterable.apply(input));
      return (Function<E, Boolean>) new Function<E, Boolean>() {

        private boolean value;

        @Override
        public Boolean apply(E entry) {
          value = Iterables.contains(collection, entry);
          return value;
        }

        @Override
        public String toString() {
          return Objects.toString(value);
        }
      };
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Value<Object> writeTo(Value<Map<String, Object>> map, Value<String> itemName, Value<Object> itemValue) {
    return input -> requireNonNull(map.apply(input)).put(itemName.apply(input), itemValue.apply(input));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Value<Object> readFrom(Value<Map<String, Object>> map, Value<String> itemName) {
    return input -> requireNonNull(map.apply(input)).get(itemName.apply(input));
  }
}
