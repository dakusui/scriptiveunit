package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

public interface SearchEngine<REQ extends Request, RESP extends Response<DOC, REQ>, DOC> extends DocAccessor<DOC> {
  RESP service(REQ request);

  REQ.Builder<REQ, ? extends REQ.Builder> requestBuilder();
}
