package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.Stage;

import static java.util.Objects.requireNonNull;

public class Strings {
  @Scriptable
  public <T extends Stage> Func<T, String> substr(Func<T, String> str, Func<T, Integer> begin, Func<T, Integer> end) {
    return input -> requireNonNull(str.apply(input)).substring(
        requireNonNull(begin.apply(input)),
        requireNonNull(end.apply(input)));
  }

  @Scriptable
  public <T extends Stage> Func<T, Integer> length(Func<T, String> str) {
    return input -> requireNonNull(str.apply(input)).length();
  }

  @Scriptable
  public <T extends Stage> Func<T, Boolean> endsWith(Func<T, String> str, Func<T, String> a) {
    return input -> requireNonNull(str.apply(input)).endsWith(requireNonNull(a.apply(input)));
  }

  @Scriptable
  public <T extends Stage> Func<T, Boolean> startsWith(Func<T, String> str, Func<T, String> a) {
    return input -> requireNonNull(str.apply(input)).startsWith(requireNonNull(a.apply(input)));
  }

  @Scriptable
  public <T extends Stage> Func<T, Boolean> matches(Func<T, String> str, Func<T, String> regex) {
    return input -> requireNonNull(str.apply(input)).matches(requireNonNull(regex.apply(input)));
  }

}
