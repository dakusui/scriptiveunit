package com.github.dakusui.scriptiveunit.model.form;

import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ValueList<T> extends Iterable<Value<T>> {
  Value<T> get(int i);

  int size();

  default Stream<Value<T>> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  static <T> ValueList<T> create(List<Value<T>> args) {
    return create(args.size(), args::get);
  }

  static <T> ValueList<T> create(int size, IntFunction<Value<T>> formCreator) {
    return new ValueList<T>() {
      @Override
      public Value<T> get(int i) {
        return formCreator.apply(i);
      }

      @Override
      public int size() {
        return size;
      }

      @SuppressWarnings("NullableProblems")
      @Override
      public Iterator<Value<T>> iterator() {
        return new Iterator<Value<T>>() {
          int i = 0;

          @Override
          public boolean hasNext() {
            return i < size();
          }

          @Override
          public Value<T> next() {
            return formCreator.apply(i++);
          }
        };
      }
    };
  }
}
