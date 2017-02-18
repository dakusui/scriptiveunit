package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.google.common.collect.Iterables;

import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

@ReflectivelyReferenced
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
      return (Iterable<? super E>) stream(requireNonNull(iterable.apply(i))
          .<E>spliterator(), false)
          .filter(input -> requireNonNull(requireNonNull(predicate.apply(i)).apply(input)))
          .collect(Collectors.<E>toList());
    };
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage, E> Func<T, Func<E, Boolean>> containedBy(Func<T, Iterable<E>> iterable) {
    return (T input) -> {
      Iterable<E> collection = requireNonNull(iterable.apply(input));
      return (Func<E, Boolean>) new Func<E, Boolean>() {
        @Override
        public Boolean apply(E entry) {
          return Iterables.contains(collection, entry);
        }
      };
    };
  }
}
