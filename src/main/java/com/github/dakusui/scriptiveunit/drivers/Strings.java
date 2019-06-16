package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Form;

import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

public class Strings {
  @SuppressWarnings("unused")
  @Scriptable
  public Form<String> substr(Form<String> str, Form<Integer> begin, Form<Integer> end) {
    return input -> requireNonNull(str.apply(input)).substring(
        requireNonNull(begin.apply(input)),
        requireNonNull(end.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Integer> length(Form<String> str) {
    return input -> requireNonNull(str.apply(input)).length();
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Boolean> endsWith(Form<String> str, Form<String> a) {
    return input -> requireNonNull(str.apply(input)).endsWith(requireNonNull(a.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Boolean> startsWith(Form<String> str, Form<String> a) {
    return input -> requireNonNull(str.apply(input)).startsWith(requireNonNull(a.apply(input)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Boolean> matches(Form<String> str, Form<String> regex) {
    return input -> requireNonNull(str.apply(input)).matches(requireNonNull(regex.apply(input)));
  }

  @SafeVarargs
  @SuppressWarnings("unused")
  @Scriptable
  public final Form<String> format(Form<String> in, Form<Object>... args) {
    return input -> String.format(requireNonNull(in.apply(input)),
        stream(args)
            .map(each -> each.apply(input))
            .collect(Collectors.toList())
            .toArray()
    );
  }
}
