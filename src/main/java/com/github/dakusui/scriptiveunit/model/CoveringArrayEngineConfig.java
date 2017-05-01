package com.github.dakusui.scriptiveunit.model;

import java.util.List;

/**
 * TODO remove this class
 */
public interface CoveringArrayEngineConfig {
  Class<?/* extends CoveringArrayEngine*/> getEngineClass();

  List<Object> getOptions();
}
