package com.github.dakusui.scriptunit.core;

import java.util.Properties;

import static com.github.dakusui.scriptunit.annotations.Load.DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY;
import static com.github.dakusui.scriptunit.annotations.Load.SCRIPT_NOT_SPECIFIED;
import static java.util.Objects.requireNonNull;

public enum SystemProperty {
  TARGET() {
    @Override
    public String getKey() {
      return DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY;
    }

    @Override
    public String getDefaultValue() {
      return SCRIPT_NOT_SPECIFIED;
    }
  };

  public String getValue(Properties properties) {
    return requireNonNull(properties).getProperty(this.getKey(), this.getDefaultValue());
  }


  abstract public String getKey();

  abstract public String getDefaultValue();
}
