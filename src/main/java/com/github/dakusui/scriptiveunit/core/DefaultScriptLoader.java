package com.github.dakusui.scriptiveunit.core;

public class DefaultScriptLoader extends BaseScriptLoader {
  private final String scriptResourceName;

  public DefaultScriptLoader(String scriptResourceName) {
    this.scriptResourceName = scriptResourceName;
  }

  @Override
  public Script<?, ?, ?, ?> load() {
    return null;
  }
}
