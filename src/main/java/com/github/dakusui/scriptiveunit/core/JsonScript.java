package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.annotations.CompatLoad;
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
    private       Reporting    reporting = new Reporting("report.json", new File("."));
    private final Object       driverObject;
    private final Properties   properties;
    final         CompatLoad   loadAnnotation;

    public Standard(Class<?> driverClass, Properties properties) {
      this.driverObject = createDriverObject(driverClass);
      this.formRegistry = this.createFormRegistry();
      this.properties = new Properties();
      this.properties.putAll(properties);
      this.loadAnnotation = ReflectionUtils.getAnnotation(driverClass, CompatLoad.class, CompatLoad.DEFAULT_INSTANCE);
    }

    public Standard(Class<?> driverClass, Properties properties, String scriptResourceName) {
      this(driverClass, properties);
      this.properties.put(this.loadAnnotation.scriptSystemPropertyKey(), scriptResourceName);
    }

    public Class getTestClass() {
      return this.driverObject.getClass();
    }

    @Override
    public FormRegistry formRegistry() {
      return this.formRegistry;
    }

    public Optional<String> getScriptResourceName() {
      String work = this.properties.getProperty(
          getScriptResourceNameKey().orElseThrow(ScriptiveUnitException::noScriptResourceNameKeyWasGiven),
          this.loadAnnotation.script());
      return CompatLoad.SCRIPT_NOT_SPECIFIED.equals(work) ?
          Optional.empty() :
          Optional.of(work);
    }

    private FormRegistry createFormRegistry() {
      return FormRegistry.getFormRegistry(this.driverObject);
    }

    private static Object createDriverObject(Class<?> driverClass) {
      try {
        return driverClass.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw ScriptiveUnitException.wrapIfNecessary(e);
      }
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
      return Optional.of(loadAnnotation.scriptSystemPropertyKey());
    }
  }
}