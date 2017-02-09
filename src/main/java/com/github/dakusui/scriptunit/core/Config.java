package com.github.dakusui.scriptunit.core;

import com.github.dakusui.scriptunit.annotations.Load;

import java.util.Properties;

import static com.github.dakusui.scriptunit.exceptions.ConfigurationException.scriptNotSpecified;
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
            Load.SCRIPT_NOT_SPECIFIED::equals,
            () -> scriptNotSpecified(getScriptSystemPropertyKey())
        );
      }
    };
  }
}
