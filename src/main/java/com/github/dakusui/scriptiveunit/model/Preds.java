package com.github.dakusui.scriptiveunit.model;

import java.util.function.Predicate;

public enum Preds {
  ;
  public static final Predicate<Object> NOT_NULL = new Predicate() {
    @Override
    public boolean test(Object o) {
      return o != null;
    }
  };

  public static <T> Predicate<T> notNull() {
    //noinspection unchecked
    return (Predicate<T>)NOT_NULL;
  }
}
