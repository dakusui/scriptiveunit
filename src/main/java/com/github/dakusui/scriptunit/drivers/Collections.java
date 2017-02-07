package com.github.dakusui.scriptunit.drivers;

import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.annotations.Scriptable;
import com.github.dakusui.scriptunit.model.Func;
import com.github.dakusui.scriptunit.model.Stage;
import com.google.common.collect.Iterables;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class Collections {
  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage, E> Func<T, Integer> size(Func<T, Iterable<? extends E>> iterable) {
    return input -> Iterables.size(requireNonNull(iterable.apply(input)));
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage, E> Func<T, Iterable<? super E>> filter(Func<T, Iterable<? extends E>> iterable, Func<T, Func<? super E, Boolean>> predicate) {
    return i -> {
      //noinspection unchecked
      return (Iterable<? super E>) stream(requireNonNull(iterable.apply(i)).<E>spliterator(), false).filter(input -> requireNonNull(requireNonNull(predicate.apply(i)).apply(input))).collect(toList());
    };
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage, E> Func<T, Func<E, Boolean>> containedBy(Func<T, Iterable<E>> iterable) {
    return input -> {
      Iterable<E> collection = requireNonNull(iterable.apply(input));
      return (Func<E, Boolean>) entry -> Iterables.contains(collection, entry);
    };
  }
}
