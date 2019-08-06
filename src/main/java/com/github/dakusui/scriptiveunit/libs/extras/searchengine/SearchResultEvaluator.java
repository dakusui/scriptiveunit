package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public interface SearchResultEvaluator<DOC> {
  double relevancyOfDocumentInIdealSearchResultAt(int position, String userQuery, List<Request.Option<?>> options);

  DocumentChecker<DOC> createDocumentCheckerFor(String userQuery, List<Request.Option<?>> options);

  interface DocumentChecker<DOC> {
    double relevancyOf(DOC doc);

    boolean isRelevant(DOC doc);

    interface PredicateBased<DOC> extends DocumentChecker<DOC> {
      @Override
      default double relevancyOf(DOC doc) {
        return isRelevant(doc) ? 1.0 : 0.0;
      }
    }

    static <DOC> DocumentChecker<DOC> createFromDocumentPredicate(Predicate<DOC> predicate) {
      requireNonNull(predicate);
      return (PredicateBased<DOC>) predicate::test;
    }
  }
}
