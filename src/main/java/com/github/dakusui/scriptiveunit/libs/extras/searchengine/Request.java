package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.github.dakusui.scriptiveunit.exceptions.Requirements.*;

public interface Request {
  interface Option<V> {
    String name();

    Optional<V> value();
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
          requireState(this.hits, isGreaterThan(0)),
          requireState(this.offset, isGreaterThanOrEqualTo(0)),
          this.options);
    }

    protected abstract REQ buildRequest(String userQuery, int hits, int offset, List<Option<?>> options);
  }
}
