package com.github.dakusui.scriptiveunit.examples.searchengine;

import java.util.List;

public class SureSearchRequest implements com.github.dakusui.scriptiveunit.libs.extras.searchengine.Request {

  @Override
  public String userQuery() {
    return null;
  }

  @Override
  public int offset() {
    return 0;
  }

  @Override
  public int hits() {
    return 0;
  }

  @Override
  public List<Option<?>> options() {
    return null;
  }
}
