package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.HostSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

import static com.github.dakusui.scriptiveunit.exceptions.ConfigurationException.scriptNotSpecified;
import static java.util.Objects.requireNonNull;

public interface Config extends IConfig<JsonNode, ObjectNode, ArrayNode, JsonNode> {
  Object getDriverObject();

  Optional<String> getScriptResourceName();

  Optional<Reporting> getReporting();

  ApplicationSpec.Dictionary readRawBaseScript();

  @Override
  default ApplicationSpec.Dictionary readScriptResource() {
    return createPreprocessor().preprocess(readRawBaseScript());
  }

  @Override
  default ApplicationSpec.Dictionary readRawScriptResource(
      String resourceName,
      HostSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> hostSpec) {
    return hostSpec.toApplicationDictionary(
        hostSpec.readObjectNode(resourceName));
  }

  @Override
  default ApplicationSpec createApplicationSpec() {
    return new ApplicationSpec.Standard();
  }

  @Override
  default HostSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> createHostSpec() {
    return new HostSpec.Json();
  }

  class Default implements Config {
    private final Object                                              driverObject;

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

    @Override
    public ApplicationSpec.Dictionary readRawBaseScript() {
      throw new UnsupportedOperationException();
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

    @Override
    public ApplicationSpec.Dictionary readRawBaseScript() {
      return base.readRawBaseScript();
    }

    @Override
    public ApplicationSpec.Dictionary readScriptResource() {
      return base.readScriptResource();
    }

    @Override
    public ApplicationSpec.Dictionary readRawScriptResource(String resourceName, HostSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> hostSpec) {
      return base.readRawScriptResource(resourceName, hostSpec);
    }

    @Override
    public ApplicationSpec createApplicationSpec() {
      return base.createApplicationSpec();
    }

    @Override
    public HostSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> createHostSpec() {
      return base.createHostSpec();
    }

    @Override
    public Preprocessor createPreprocessor() {
      return base.createPreprocessor();
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
      private final Builder   builder;

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

      @Override
      public ApplicationSpec.Dictionary readRawBaseScript() {
        return createHostSpec()
            .readRawScript(
                getScriptResourceName().orElseThrow(() -> scriptNotSpecified(this)));
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