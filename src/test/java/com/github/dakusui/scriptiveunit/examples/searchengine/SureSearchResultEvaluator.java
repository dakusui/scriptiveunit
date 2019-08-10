package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.libs.extras.searchengine.Request;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.SearchResultEvaluator;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.Dictionary;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class SureSearchResultEvaluator implements SearchResultEvaluator<Dictionary>, SureSearchDocAccessor {
  private final SureSearchDocSet docSet;

  public SureSearchResultEvaluator(SureSearchDocSet docSet) {
    this.docSet = docSet;
  }

  @Override
  public double relevancyOfDocumentInIdealSearchResultAt(int position, String userQuery, List<Request.Option<?>> options) {
    List<SureSearchDoc> relevantDocs = this.docSet.find(doc -> doc.relevancyWith(userQuery) > 0)
        .stream()
        .sorted((o1, o2) -> (int) (o2.relevancyWith(userQuery) - o1.relevancyWith(userQuery)))
        .collect(toList());
    return position < relevantDocs.size() ?
        relevantDocs.get(position).relevancyWith(userQuery) :
        0;
  }

  @Override
  public DocumentChecker<Dictionary> createDocumentCheckerFor(String userQuery, List<Request.Option<?>> options) {
    return new DocumentChecker<Dictionary>() {
      @Override
      public double relevancyOf(Dictionary doc) {
        return docSet.lookUp(idOf(doc)).map(d -> d.relevancyWith(userQuery)).orElseThrow(RuntimeException::new);
      }

      @Override
      public boolean isRelevant(Dictionary doc) {
        return relevancyOf(doc) > 0;
      }
    };
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }
}
