package com.github.dakusui.scriptiveunit.libs.extras;

import java.util.List;
import java.util.Optional;

import static com.github.dakusui.scriptiveunit.exceptions.Requirements.*;

public interface SearchEngine<REQ extends SearchEngine.Request, RESP extends SearchEngine.Response<DOC>, DOC> {

  RESP service(REQ request);

  REQ.Builder<REQ, ? extends REQ.Builder> requestBuilder();

  String idOf(DOC doc);

  Optional<?> valueOf(DOC doc, String fieldName);

  interface Request {

    abstract class Builder<REQ extends Request, B extends Builder<REQ, B>> {
      private String userQuery = null;
      private int    offset    = 0;
      private int    hits      = 0;

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

      public REQ build() {
        return buildRequest(
            requireState(this.userQuery, isNonNull()),
            requireState(this.hits, isGreaterThan(0)),
            requireState(this.offset, isGreaterThanOrEqualTo(0)));
      }

      protected abstract REQ buildRequest(String userQuery, int hits, int offset);
    }
  }

  interface Response<DOC> {
    boolean wasSuccessful();

    List<DOC> docs();
  }
}
