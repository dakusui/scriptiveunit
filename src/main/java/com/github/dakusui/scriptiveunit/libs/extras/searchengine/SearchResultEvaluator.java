package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.List;
import java.util.function.Predicate;

import static com.github.dakusui.scriptiveunit.libs.extras.searchengine.SearchResultEvaluator.DocumentChecker.createFromDocumentPredicate;
import static java.util.Objects.requireNonNull;

public interface SearchResultEvaluator<DOC> {
  double relevancyOfDocumentInIdealSearchResultAt(int position, String userQuery, List<Request.Option<?>> options);

  DocumentChecker<DOC> createDocumentCheckerFor(String userQuery, List<Request.Option<?>> options);

  static <DOC> SearchResultEvaluator<DOC> createSearchResultEvaluatorFromDocPredicate(
      final String name,
      Predicate<DOC> predicate) {
    return createSearchResultEvaluatorFromDocumentChecker(name, createFromDocumentPredicate(predicate));
  }

  static <DOC> SearchResultEvaluator<DOC> createSearchResultEvaluatorFromDocumentChecker(
      final String name,
      DocumentChecker<DOC> documentChecker) {
    return createSearchResultEvaluatorFromDocumentCheckerFactory(name, ((userQuery, options) -> documentChecker));
  }

  static <DOC> SearchResultEvaluator<DOC> createSearchResultEvaluatorFromDocumentCheckerFactory(
      final String name,
      DocumentChecker.Factory<DOC> documentCheckerFactory) {
    return new Default<DOC>() {
      @Override
      public DocumentChecker<DOC> createDocumentCheckerFor(String userQuery, List<Request.Option<?>> options) {
        return documentCheckerFactory.create(userQuery, options);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  /**
   * A default {@link SearchResultEvaluator} interface, which always returns 1.0 on {@code relevancyOfDocumentInIdealSearchResultAt} method call.
   * This means the search engine under evaluation has infinite number of ideal documents on any search query.
   *
   * @param <DOC> A type that is handled by the search engine under evaluation.
   */
  interface Default<DOC> extends SearchResultEvaluator<DOC> {
    default double relevancyOfDocumentInIdealSearchResultAt(int position, String userQuery, List<Request.Option<?>> options) {
      return 1.0;
    }
  }

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

    interface Factory<DOC> {
      DocumentChecker<DOC> create(String userQuery, List<Request.Option<?>> options);
    }
  }
}
