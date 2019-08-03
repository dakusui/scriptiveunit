package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.List;

public interface SearchResultEvaluator<DOC> {
  double relevancyOfDocumentInIdealSearchResultAt(int position, String userQuery, List<Request.Option<?>> options);

  double relevancyOf(DOC doc, String userQuery, List<Request.Option<?>> options);

  boolean isRelevant(DOC doc, String userQuery, List<Request.Option<?>> options);
}
