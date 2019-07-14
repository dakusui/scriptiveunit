package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

public interface Config {
  Object getDriverObject();

  String getScriptResourceNameKey();

  Optional<String> getScriptResourceName();

  Reporting getReporting();

  class Default implements Config {
    private final Object driverObject;

    public Default(Object driverObject) {
      this.driverObject = driverObject;
    }

    @Override
    public Object getDriverObject() {
      return this.driverObject;
    }

    @Override
    public String getScriptResourceNameKey() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getScriptResourceName() {
      return Optional.empty();
    }

    @Override
    public Reporting getReporting() {
      return null;
    }

    static Config create(Object driverObject) {
      return new Default(driverObject);
    }
  }

  class Delegating implements Config {
    private final Config base;

    protected Delegating(Config base) {
      this.base = base;
    }

    protected Config base() {
      return this.base;
    }

    @Override
    public Object getDriverObject() {
      return base.getDriverObject();
    }

    @Override
    public String getScriptResourceNameKey() {
      return base.getScriptResourceNameKey();
    }

    @Override
    public Optional<String> getScriptResourceName() {
      return base.getScriptResourceName();
    }

    @Override
    public Reporting getReporting() {
      return base.getReporting();
    }
  }

  class Builder {
    private final Properties properties;
    final         Load       loadAnnotation;
    private final Class<?>   driverClass;

    public Builder(Class<?> driverClass, Properties properties) {
      this.driverClass = driverClass;
      this.properties = new Properties();
      this.properties.putAll(properties);
      this.loadAnnotation = ReflectionUtils.getAnnotation(driverClass, Load.class, Load.DEFAULT_INSTANCE);
    }

    public Builder withScriptResourceName(String scriptResourceName) {
      this.properties.put(loadAnnotation.scriptSystemPropertyKey(), scriptResourceName);
      return this;
    }

    public Config build() {
      try {
        return new Config() {
          private Reporting reporting = new Reporting("report.json", new File("."));
          Object driverObject = Builder.this.driverClass.newInstance();

          @Override
          public Object getDriverObject() {
            return driverObject;
          }

          @Override
          public String getScriptResourceNameKey() {
            return loadAnnotation.scriptSystemPropertyKey();
          }

          @Override
          public Optional<String> getScriptResourceName() {
            String work = properties.getProperty(
                getScriptResourceNameKey(),
                Builder.this.loadAnnotation.script());
            return Load.SCRIPT_NOT_SPECIFIED.equals(work) ?
                Optional.empty() :
                Optional.of(work);
          }

          @Override
          public Reporting getReporting() {
            return reporting;
          }
        };
      } catch (InstantiationException | IllegalAccessException e) {
        throw ScriptiveUnitException.wrapIfNecessary(e);
      }
    }
  }
}