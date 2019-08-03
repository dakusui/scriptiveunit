package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.libs.extras.searchengine.Request;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.SearchEngine;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;

import java.util.Optional;

public class
SureSearch implements SearchEngine<SureSearchRequest, SureSearchResponse, ApplicationSpec.Dictionary> {
  @Override
  public SureSearchResponse service(SureSearchRequest request) {
    return null;
  }

  @Override
  public Request.Builder<SureSearchRequest, ? extends Request.Builder> requestBuilder() {
    return null;
  }

  @Override
  public String idOf(ApplicationSpec.Dictionary dictionary) {
    return null;
  }

  @Override
  public Optional<?> valueOf(ApplicationSpec.Dictionary dictionary, String fieldName) {
    return Optional.empty();
  }
}
