package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.libs.extras.searchengine.Response;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;

import java.util.List;

public class SureSearchResponse implements Response<ApplicationSpec.Dictionary, SureSearchRequest> {
  @Override
  public SureSearchRequest request() {
    return null;
  }

  @Override
  public List<ApplicationSpec.Dictionary> docs() {
    return null;
  }
}
