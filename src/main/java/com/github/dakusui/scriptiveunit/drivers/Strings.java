package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;

import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class Strings {
  @SuppressWarnings("unused")
  @Scriptable
  public Value<String> substr(Value<String> str, Value<Integer> begin, Value<Integer> end) {
    return input -> requireNonNull(str.apply(input)).substring(
        requireNonNull(begin.apply(input)),
        requireNonNull(end.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Value<Integer> length(Value<String> str) {
    return input -> requireNonNull(str.apply(input)).length();
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Value<Boolean> endsWith(Value<String> str, Value<String> a) {
    return input -> requireNonNull(str.apply(input)).endsWith(requireNonNull(a.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Value<Boolean> startsWith(Value<String> str, Value<String> a) {
    return input -> requireNonNull(str.apply(input)).startsWith(requireNonNull(a.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Value<Boolean> matches(Value<String> str, Value<String> regex) {
    return input -> requireNonNull(str.apply(input)).matches(requireNonNull(regex.apply(input)));
  }

  @Scriptable
  public final Value<String> format(Value<String> in, ValueList<Object> args) {
    return input -> String.format(requireNonNull(in.apply(input)),
        args.stream()
            .map(each -> each.apply(input))
            .collect(Collectors.toList())
            .toArray()
    );
  }
}
