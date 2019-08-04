package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.libs.extras.searchengine.Response;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;

import java.util.Formatter;
import java.util.List;

public class SureSearchResponse implements Response<ApplicationSpec.Dictionary, SureSearchRequest> {
  private final SureSearchRequest                request;
  private final List<ApplicationSpec.Dictionary> docs;

  SureSearchResponse(SureSearchRequest request, List<ApplicationSpec.Dictionary> docs) {
    this.request = request;
    this.docs = docs;
  }

  @Override
  public SureSearchRequest request() {
    return this.request;
  }

  @Override
  public List<ApplicationSpec.Dictionary> docs() {
    return this.docs;
  }
}
