package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.libs.extras.searchengine.Request;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.SearchResultEvaluator;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.Dictionary;

import java.util.List;

import static java.util.stream.Collectors.toList;

class SureSearchResultEvaluator implements SearchResultEvaluator<Dictionary>, SureSearchDocAccessor {
  private final SureSearchDocSet docSet;

  SureSearchResultEvaluator(SureSearchDocSet docSet) {
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
  public double relevancyOf(Dictionary doc, String userQuery, List<Request.Option<?>> options) {
    return docSet.lookUp(idOf(doc)).map(d -> d.relevancyWith(userQuery)).orElseThrow(RuntimeException::new);
  }

  @Override
  public boolean isRelevant(Dictionary doc, String userQuery, List<Request.Option<?>> options) {
    return relevancyOf(doc, userQuery, options) > 0;
  }
}
