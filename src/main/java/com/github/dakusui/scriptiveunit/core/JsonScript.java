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

  class Delegating implements JsonScript {
    private final JsonScript base;

    protected Delegating(JsonScript base) {
      this.base = base;
    }

    protected JsonScript base() {
      return this.base;
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
    public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
      return base.languageSpec();
    }

    @Override
    public ApplicationSpec applicationSpec() {
      return base.applicationSpec();
    }

    @Override
    public HostSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> hostSpec() {
      return base.hostSpec();
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

  abstract class Base implements JsonScript {
    private final LanguageSpec.ForJson languageSpec;

    Base(LanguageSpec.ForJson languageSpec) {
      this.languageSpec = requireNonNull(languageSpec);
    }

    @Override
    public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
      return this.languageSpec;
    }
  }

  class Default extends Base {

    private final LanguageSpec.ForJson languageSpec;
    private final Reporting            reporting;
    private final String               scriptResourceName;

    Default(LanguageSpec.ForJson languageSpec, Reporting reporting, String scriptResourceName) {
      super(languageSpec);
      this.languageSpec = languageSpec;
      this.reporting = reporting;
      this.scriptResourceName = scriptResourceName;
    }

    @Override
    public Optional<Reporting> getReporting() {
      return Optional.ofNullable(reporting);
    }

    @Override
    public ApplicationSpec.Dictionary readRawBaseScript() {
      return hostSpec().readRawScript(scriptResourceName);
    }

    @Override
    public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
      return languageSpec;
    }

    static Default createFromResource(
        String resourceName,
        FormRegistry formRegistry,
        Reporting reporting) {
      return new Default(
          LanguageSpec.ForJson.create(formRegistry),
          reporting,
          resourceName);
    }

    static Default createFromResourceSpecifiedByPropertyKey(
        String propertyKey,
        FormRegistry formRegistry,
        Reporting reporting,
        Properties properties) {
      return createFromResource(
          properties.getProperty(propertyKey),
          formRegistry,
          reporting
      );
    }

    static Default createFromResourceSpecifiedBySystemPropertyKey(
        String systemPropertyKey,
        FormRegistry formRegistry,
        Reporting reporting) {
      return createFromResourceSpecifiedByPropertyKey(
          systemPropertyKey,
          formRegistry,
          reporting,
          System.getProperties());
    }
  }

  class Compat extends Base {
    private       Reporting  reporting;
    private final Class<?>   driverClass;
    private final Properties properties;
    private       String     scriptResourceNameKey;

    public Compat(Class<?> driverClass, Properties properties) {
      super(createLanguageSpecFrom(createDriverObject(driverClass)));
      this.driverClass = driverClass;
      this.properties = new Properties();
      this.properties.putAll(properties);
      final CompatLoad loadAnnotation = ReflectionUtils.getAnnotation(
          driverClass,
          CompatLoad.class,
          CompatLoad.DEFAULT_INSTANCE);
      this.scriptResourceNameKey = loadAnnotation.scriptSystemPropertyKey();
      this.reporting = new Reporting("report.json", new File("."));
    }

    public Compat(Class<?> driverClass, Properties properties, String scriptResourceName) {
      this(driverClass, properties);
      final CompatLoad loadAnnotation = ReflectionUtils.getAnnotation(
          driverClass,
          CompatLoad.class,
          CompatLoad.DEFAULT_INSTANCE);
      this.properties.put(loadAnnotation.scriptSystemPropertyKey(), scriptResourceName);
    }

    public String getScriptResourceNameKey() {
      return scriptResourceNameKey;
    }

    public Optional<String> getScriptResourceName() {
      String work = this.properties.getProperty(
          getScriptResourceNameKey(),
          CompatLoad.SCRIPT_NOT_SPECIFIED);
      return CompatLoad.SCRIPT_NOT_SPECIFIED.equals(work) ?
          Optional.empty() :
          Optional.of(work);
    }

    public Class getTestClass() {
      return this.driverClass;
    }

    @Override
    public Optional<Reporting> getReporting() {
      return Optional.of(reporting);
    }

    @Override
    public ApplicationSpec.Dictionary readRawBaseScript() {
      return hostSpec()
          .readRawScript(
              getScriptResourceName().orElseThrow(() -> scriptNotSpecified(this)));
    }

    private static LanguageSpec.ForJson createLanguageSpecFrom(Object driverObject) {
      return LanguageSpec.ForJson.create(FormRegistry.getFormRegistry(driverObject));
    }

    private static Object createDriverObject(Class<?> driverClass) {
      try {
        return driverClass.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw ScriptiveUnitException.wrapIfNecessary(e);
      }
    }
  }
}