package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Form;
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
  public <E> Form<Integer> size(Form<Iterable<? extends E>> iterable) {
    return (Stage input) -> Iterables.size(requireNonNull(iterable.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <E> Form<Integer> concat(Form<Iterable<? extends E>> iterable) {
    return (Stage input) -> Iterables.size(requireNonNull(iterable.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <E> Form<Iterable<? extends E>> compatFilter(Form<Iterable<? extends E>> iterable, Form<Function<E, Boolean>> predicate) {
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
  public <E> Form<Iterable<? extends E>> filter(Form<Iterable<? extends E>> iterable, Form<Form<Boolean>> predicate) {
    return (Stage i) -> (Iterable<? extends E>) stream(
        requireNonNull(iterable.apply(i)).spliterator(),
        false
    ).filter(
        (E entry) -> predicate.apply(i).apply(wrapValueAsArgumentInStage(i, toFunc(entry)))
    ).collect(
        Collectors.<E>toList()
    );
  }

  private static <F> Form<F> toFunc(F entry) {
    return input -> entry;
  }

  public static <E> Stage wrapValueAsArgumentInStage(Stage i, Form<E> value) {
    return Stage.Factory.createWrappedStage(i, value);
  }

  @SuppressWarnings("unused")
  @Scriptable
  public <E> Form<Function<E, Boolean>> containedBy(Form<Iterable<E>> iterable) {
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
  public Form<Object> writeTo(Form<Map<String, Object>> map, Form<String> itemName, Form<Object> itemValue) {
    return input -> requireNonNull(map.apply(input)).put(itemName.apply(input), itemValue.apply(input));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Object> readFrom(Form<Map<String, Object>> map, Form<String> itemName) {
    return input -> requireNonNull(map.apply(input)).get(itemName.apply(input));
  }
}
