package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.List;

public interface Response<DOC> {
  boolean wasSuccessful();

  List<DOC> docs();
}
