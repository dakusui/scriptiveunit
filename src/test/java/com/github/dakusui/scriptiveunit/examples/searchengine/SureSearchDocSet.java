package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;

public enum SureSearchDocSet {
  DEFAULT();

  private final ApplicationSpec.Dictionary[] docs;

  private SureSearchDocSet(ApplicationSpec.Dictionary... docs) {
    this.docs = docs;
  }
}
