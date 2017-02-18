package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;

import java.util.List;

public interface CoveringArrayEngineConfig {
  Class<? extends CoveringArrayEngine> getEngineClass();

  List<Object> getOptions();
}
