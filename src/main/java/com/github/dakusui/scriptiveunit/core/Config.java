package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public interface Config {
  Object getDriverObject();

  Optional<String> getScriptResourceName();

  Optional<Reporting> getReporting();

  class Default implements Config {
    private final Object driverObject;

    Default(Object driverObject) {
      this.driverObject = driverObject;
    }

    @Override
    public Object getDriverObject() {
      return this.driverObject;
    }

    @Override
    public Optional<String> getScriptResourceName() {
      return Optional.empty();
    }

    @Override
    public Optional<Reporting> getReporting() {
      return Optional.empty();
    }

    public static Config create(Object driverObject) {
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
    public Optional<String> getScriptResourceName() {
      return base.getScriptResourceName();
    }

    @Override
    public Optional<Reporting> getReporting() {
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

    public DriverClassBasedConfig build() {
      return new DriverClassBasedConfig(this);
    }

    public static class DriverClassBasedConfig implements Config {
      private       Reporting reporting = new Reporting("report.json", new File("."));
      private final Object    driverObject;
      ;
      private final Builder builder;

      DriverClassBasedConfig(Builder builder) {
        this.builder = requireNonNull(builder);
        this.driverObject = createDriverObject(this.builder);
      }

      @Override
      public Object getDriverObject() {
        return driverObject;
      }

      @Override
      public Optional<String> getScriptResourceName() {
        String work = builder.properties.getProperty(
            getScriptResourceNameKey().orElseThrow(ScriptiveUnitException::noScriptResourceNameKeyWasGiven),
            builder.loadAnnotation.script());
        return Load.SCRIPT_NOT_SPECIFIED.equals(work) ?
            Optional.empty() :
            Optional.of(work);
      }

      @Override
      public Optional<Reporting> getReporting() {
        return Optional.of(reporting);
      }

      public Optional<String> getScriptResourceNameKey() {
        return Optional.of(builder.loadAnnotation.scriptSystemPropertyKey());
      }

      private static Object createDriverObject(Builder builder) {
        try {
          return builder.driverClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
          throw ScriptiveUnitException.wrapIfNecessary(e);
        }
      }
    }
  }
}