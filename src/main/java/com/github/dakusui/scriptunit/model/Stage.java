package com.github.dakusui.scriptunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import static java.util.Objects.requireNonNull;

public interface Stage {
  Tuple getFixture();

  <RESPONSE> RESPONSE response();

  Type getType();

  enum Type {
    GIVEN,
    WHEN,
    THEN;

    public Stage create(Tuple fixture) {
      return _create(fixture, null);
    }

    public Stage create(Tuple fixture, Object response) {
      return _create(fixture, requireNonNull(response));
    }

    private Stage _create(Tuple fixture, Object response) {
      Type type = this;
      return new Stage() {
        @Override
        public Tuple getFixture() {
          return fixture;
        }

        @Override
        public <RESPONSE> RESPONSE response() {
          if (response == null)
            throw new UnsupportedOperationException();
          //noinspection unchecked
          return (RESPONSE) response;
        }

        @Override
        public Type getType() {
          return type;
        }
      };
    }
  }
}
