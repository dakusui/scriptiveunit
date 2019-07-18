package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.HostSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.form.FormRegistry;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

import static com.github.dakusui.scriptiveunit.exceptions.ConfigurationException.scriptNotSpecified;
import static java.util.Objects.requireNonNull;

public interface JsonScript extends Script<JsonNode, ObjectNode, ArrayNode, JsonNode> {
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

  class Default implements JsonScript {
    private final FormRegistry formRegistry;

    Default(FormRegistry formRegistry) {
      this.formRegistry = formRegistry;
    }

    @Override
    public FormRegistry formRegistry() {
      return this.formRegistry;
    }

    @Override
    public Optional<Reporting> getReporting() {
      return Optional.empty();
    }

    @Override
    public ApplicationSpec.Dictionary readRawBaseScript() {
      throw new UnsupportedOperationException();
    }

    public static JsonScript create(FormRegistry formRegistry) {
      return new Default(formRegistry);
    }
  }

  class Delegating implements JsonScript {
    private final JsonScript base;

    protected Delegating(JsonScript base) {
      this.base = base;
    }

    protected JsonScript base() {
      return this.base;
    }

    @Override
    public FormRegistry formRegistry() {
      return base.formRegistry();
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
    public String name() {
      return base.name();
    }

    @Override
    public Preprocessor createPreprocessor() {
      return base.createPreprocessor();
    }
  }

  class Standard implements JsonScript {
    private final FormRegistry formRegistry;
    private Reporting reporting = new Reporting("report.json", new File("."));
    private final Object driverObject;
    private final Builder builder;

    Standard(Builder builder) {
      this.builder = requireNonNull(builder);
      this.driverObject = createDriverObject(this.builder);
      this.formRegistry = FormRegistry.getFormRegistry(driverObject);
    }

      public Class getTestClass() {
        return this.driverObject.getClass();
      }

      @Override
      public FormRegistry formRegistry() {
        return this.formRegistry;
      }

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

    public static class Builder {
      private final Properties properties;
      final Load loadAnnotation;
      private final Class<?> driverClass;

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

      public Standard build() {
        return new Standard(this);
      }

    }
  }
}