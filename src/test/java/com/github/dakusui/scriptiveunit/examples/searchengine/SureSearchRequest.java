package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.libs.extras.searchengine.Request;

import java.util.List;

public class SureSearchRequest implements Request {

  private final List<Option<?>> options;
  private final String userQuery;
  private final int offset;
  private final int hits;

  private SureSearchRequest(String userQuery, List<Option<?>> options, int offset, int hits) {
    this.userQuery = userQuery;
    this.options = options;
    this.offset = offset;
    this.hits = hits;
  }

  @Override
  public String userQuery() {
    return this.userQuery;
  }

  @Override
  public int offset() {
    return this.offset;
  }

  @Override
  public int hits() {
    return this.hits;
  }

  @Override
  public List<Option<?>> options() {
    return this.options;
  }

  static class Builder extends Request.Builder<SureSearchRequest, Builder> {
    @Override
    protected SureSearchRequest buildRequest(String userQuery, List<Option<?>> options, int hits, int offset) {
      return new SureSearchRequest(userQuery, options, offset, hits);
    }
  }
}
