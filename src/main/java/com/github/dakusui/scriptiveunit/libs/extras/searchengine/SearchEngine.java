package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import jdk.nashorn.internal.runtime.options.Option;

import java.util.Optional;

public interface SearchEngine<REQ extends Request, RESP extends Response<DOC>, DOC> {

  RESP service(REQ request);

  REQ.Builder<REQ, ? extends REQ.Builder> requestBuilder();

  String idOf(DOC doc);

  Optional<?> valueOf(DOC doc, String fieldName);

  interface Evaluator<DOC> {
    double relevancyOfDocumentInIdealSearchResultAt(int position, String userQuery, Option<?>... options);

    int numRelevantDocumentsFor(int position, String userQuery, Option<?>... options);

    double relevancyOf(DOC doc, String userQuery, Option<?>... options);

    boolean isRelevant(DOC doc, String userQuery, Option<?>... options);
  }
}
