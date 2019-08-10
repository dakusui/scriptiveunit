package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.libs.extras.searchengine.SearchEngine;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.Dictionary;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class SureSearch implements SearchEngine<SureSearchRequest, SureSearchResponse, Dictionary> , SureSearchDocAccessor {
  private final List<Dictionary> docs;

  public SureSearch(List<Dictionary> docs) {
    this.docs = docs;
  }

  @Override
  public SureSearchResponse service(SureSearchRequest request) {
    return new SureSearchResponse(
        request,
        this.docs.stream()
            .filter(doc -> descriptionOf(doc).contains(request.userQuery()))
            .collect(toList())
    );
  }

  @Override
  public SureSearchRequest.Builder requestBuilder() {
    return new SureSearchRequest.Builder();
  }
}
