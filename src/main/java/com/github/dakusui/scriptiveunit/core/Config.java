package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.annotations.Load;

import java.util.Properties;

import static com.github.dakusui.scriptiveunit.exceptions.ConfigurationException.scriptNotSpecified;

public interface Config {
  Class<?> getDriverClass();

  String getScriptResourceNameKey();

  String getScriptResourceName();

  class Builder {
    private final Properties properties;
    final         Load       loadAnnotation;
    private final Class<?>   driverClass;

    public Builder(Class<?> driverClass, Properties properties) {
      this.driverClass = driverClass;
      this.properties = properties;
      this.loadAnnotation = Utils.getAnnotation(driverClass, Load.class, Load.DEFAULT_INSTANCE);
    }

    public Builder withScriptResourceName(String scriptResourceName) {
      this.properties.put(loadAnnotation.scriptSystemPropertyKey(), scriptResourceName);
      return this;
    }

    public Config build() {
      return new Config() {
        @Override
        public Class<?> getDriverClass() {
          return Builder.this.driverClass;
        }

        @Override
        public String getScriptResourceNameKey() {
          return loadAnnotation.scriptSystemPropertyKey();
        }

        @Override
        public String getScriptResourceName() {
          return Utils.check(
              properties.getProperty(getScriptResourceNameKey(), Builder.this.loadAnnotation.defaultScriptName()),
              (in) -> !in.equals(Load.SCRIPT_NOT_SPECIFIED),
              () -> scriptNotSpecified(getScriptResourceNameKey())
          );
        }
      };
    }
  }
}
