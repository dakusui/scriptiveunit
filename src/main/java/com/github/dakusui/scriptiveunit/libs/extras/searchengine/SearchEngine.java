package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.Optional;

public interface SearchEngine<REQ extends Request, RESP extends Response<DOC, REQ>, DOC> {
  RESP service(REQ request);

  REQ.Builder<REQ, ? extends REQ.Builder> requestBuilder();

  String idOf(DOC doc);

  Optional<?> valueOf(DOC doc, String fieldName);
}
