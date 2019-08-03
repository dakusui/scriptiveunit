package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.libs.extras.searchengine.Request;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.SearchResultEvaluator;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;

import java.util.List;

class SureSearchResultEvaluator implements SearchResultEvaluator<ApplicationSpec.Dictionary> {
  @Override
  public double relevancyOfDocumentInIdealSearchResultAt(int position, String userQuery, List<Request.Option<?>> options) {
    return 0;
  }

  @Override
  public double relevancyOf(ApplicationSpec.Dictionary dictionary, String userQuery, List<Request.Option<?>> options) {
    return 0;
  }

  @Override
  public boolean isRelevant(ApplicationSpec.Dictionary dictionary, String userQuery, List<Request.Option<?>> options) {
    return false;
  }
}
