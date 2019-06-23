package com.github.dakusui.scriptiveunit.model.form;

import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface FormList<T> extends Iterable<Form<T>> {
  Form<T> get(int i);

  int size();

  default Stream<Form<T>> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  static <T> FormList<T> create(List<Form<T>> args) {
    return create(args.size(), args::get);
  }

  static <T> FormList<T> create(int size, IntFunction<Form<T>> formCreator) {
    return new FormList<T>() {
      @Override
      public Form<T> get(int i) {
        return formCreator.apply(i);
      }

      @Override
      public int size() {
        return size;
      }

      @SuppressWarnings("NullableProblems")
      @Override
      public Iterator<Form<T>> iterator() {
        return new Iterator<Form<T>>() {
          int i = 0;

          @Override
          public boolean hasNext() {
            return i < size();
          }

          @Override
          public Form<T> next() {
            return formCreator.apply(i++);
          }
        };
      }
    };
  }
}
