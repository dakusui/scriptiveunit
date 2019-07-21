package com.github.dakusui.scriptiveunit.core;

public interface ScriptLoader {
  Script<?, ?, ?, ?> load();

  abstract class Base implements ScriptLoader {
  }

  class FromStandardSystemProperty extends Base {
    private final String scriptResourceName;

    public FromStandardSystemProperty(String scriptResourceName) {
      this.scriptResourceName = scriptResourceName;
    }

    @Override
    public Script<?, ?, ?, ?> load() {
      return null;
    }
  }
}
