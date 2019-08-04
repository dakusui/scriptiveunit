package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.*;

import static com.github.dakusui.scriptiveunit.exceptions.Requirements.*;

public interface Request extends Formattable {
  String userQuery();

  int offset();

  int hits();

  List<Option<?>> options();

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("%s:{userQuery=<%s>,offset=<%s>,hits=<%s>,options:<%s>}",
        this.getClass().getSimpleName(),
        this.userQuery(),
        this.offset(),
        this.hits(),
        options()
    );
  }

  interface Option<V> {
    String name();

    Optional<V> value();

    static <T> Option<T> create(final String name, final T value) {
      return new Option<T>() {
        @Override
        public String name() {
          return name;
        }

        @Override
        public Optional<T> value() {
          return Optional.of(value);
        }
      };
    }

    static <T> Option<T> empty(final String name) {
      return new Option<T>() {
        @Override
        public String name() {
          return name;
        }

        @Override
        public Optional<T> value() {
          return Optional.empty();
        }
      };
    }
  }

  abstract class Builder<REQ extends Request, B extends Builder<REQ, B>> {
    private String          userQuery = null;
    private int             offset    = 0;
    private int             hits      = 0;
    private List<Option<?>> options   = new LinkedList<>();

    @SuppressWarnings("unchecked")
    public B userQuery(String userQuery) {
      this.userQuery = userQuery;
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B offset(int offset) {
      this.offset = offset;
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B hits(int hits) {
      this.hits = hits;
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B addOptions(List<Option<?>> options) {
      this.options.addAll(options);
      return (B) this;
    }

    public REQ build() {
      return buildRequest(
          requireState(this.userQuery, isNonNull()),
          this.options, requireState(this.hits, isGreaterThan(0)),
          requireState(this.offset, isGreaterThanOrEqualTo(0))
      );
    }

    protected abstract REQ buildRequest(String userQuery, List<Option<?>> options, int hits, int offset);
  }
}
