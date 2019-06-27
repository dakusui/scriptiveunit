package com.github.dakusui.scriptiveunit.loaders.json;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ModelSpec {
  Dictionary createDefaultValues();

  interface Node {
  }

  interface Atom extends Node {
    <T> T get();
  }

  interface Array extends Node, Iterable<Node> {
    Node get(int i);

    int size();

    @SuppressWarnings("NullableProblems")
    @Override
    default Iterator<Node> iterator() {
      return new Iterator<Node>() {
        int i = 0;

        @Override
        public boolean hasNext() {
          return i < size();
        }

        @Override
        public Node next() {
          return get(i++);
        }
      };
    }

    default Stream<Node> stream() {
      return StreamSupport.stream(this.spliterator(), false);
    }
  }

  interface Dictionary extends Node {
    Iterable<String> keys();

    Node valueOf(String key);

    int size();
  }
}
