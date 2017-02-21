package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.func.Func;

import static java.util.Objects.requireNonNull;

public class Strings {
  @ReflectivelyReferenced
  @Scriptable
  public Func<String> substr(Func<String> str, Func<Integer> begin, Func<Integer> end) {
    return input -> requireNonNull(str.apply(input)).substring(
        requireNonNull(begin.apply(input)),
        requireNonNull(end.apply(input)));
  }

  @ReflectivelyReferenced
  @Scriptable
  public Func<Integer> length(Func<String> str) {
    return input -> requireNonNull(str.apply(input)).length();
  }

  @ReflectivelyReferenced
  @Scriptable
  public Func<Boolean> endsWith(Func<String> str, Func<String> a) {
    return input -> requireNonNull(str.apply(input)).endsWith(requireNonNull(a.apply(input)));
  }

  @ReflectivelyReferenced
  @Scriptable
  public Func<Boolean> startsWith(Func<String> str, Func<String> a) {
    return input -> requireNonNull(str.apply(input)).startsWith(requireNonNull(a.apply(input)));
  }

  @ReflectivelyReferenced
  @Scriptable
  public Func<Boolean> matches(Func<String> str, Func<String> regex) {
    return input -> requireNonNull(str.apply(input)).matches(requireNonNull(regex.apply(input)));
  }

}
