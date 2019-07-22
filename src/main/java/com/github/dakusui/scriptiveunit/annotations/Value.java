package com.github.dakusui.scriptiveunit.annotations;

import java.lang.annotation.Retention;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface Value {
  String[] value() default {};

  Class<? extends Converter> type() default StringValue.class;

  interface Converter<V> extends Function<String[], Optional<V>> {
  }

  class StringValue implements Converter<String> {
    @Override
    public Optional<String> apply(String[] s) {
      return s.length == 0 ?
          Optional.empty() :
          Optional.of(s[0]);
    }
  }
}
