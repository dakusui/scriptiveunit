package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.annotations.Load;

import java.util.Properties;

import static com.github.dakusui.scriptiveunit.exceptions.ConfigurationException.scriptNotSpecified;
import static java.util.Objects.requireNonNull;

public interface Config {
  String getScriptSystemPropertyKey();

  String getScriptResourceName();

  static Config create(Class<?> testClass, Properties properties) {
    requireNonNull(testClass);
    requireNonNull(properties);
    return new Config() {
      final Load loadAnnotation = Utils.getAnnotation(testClass, Load.class, Load.DEFAULT_INSTANCE);

      @Override
      public String getScriptSystemPropertyKey() {
        return loadAnnotation.scriptSystemPropertyKey();
      }

      @Override
      public String getScriptResourceName() {
        return Utils.check(
            properties.getProperty(getScriptSystemPropertyKey(), this.loadAnnotation.defaultScriptName()),
            (in) -> !in.equals(Load.SCRIPT_NOT_SPECIFIED),
            () -> scriptNotSpecified(getScriptSystemPropertyKey())
        );
      }
    };
  }
}
