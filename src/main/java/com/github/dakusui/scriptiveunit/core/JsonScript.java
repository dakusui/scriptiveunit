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
    private final FormRegistry formRegistry;

    Base(FormRegistry formRegistry) {
      this.formRegistry = formRegistry;
    }

    @Override
    public FormRegistry formRegistry() {
      return this.formRegistry;
    }

  }

  class Default2 extends Base {

    private final LanguageSpec.ForJson languageSpec;
    private final Reporting            reporting;

    Default2(LanguageSpec.ForJson languageSpec, Reporting reporting) {
      super(languageSpec.formRegistry());
      this.languageSpec = languageSpec;
      this.reporting = reporting;
    }

    @Override
    public Optional<Reporting> getReporting() {
      return Optional.ofNullable(reporting);
    }

    @Override
    public ApplicationSpec.Dictionary readRawBaseScript() {
      return null;
    }

    @Override
    public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
      return languageSpec;
    }

    static Default2 createFromJsonScriptResource(
        String resourceName,
        FormRegistry formRegistry,
        Reporting reporting) {
      return new Default2(
          LanguageSpec.ForJson.create(formRegistry),
          reporting);
    }

  }

  class Default extends Base {

    private final ApplicationSpec.Dictionary scriptBody;
    private final Reporting                  reporting;
    private final LanguageSpec.ForJson       languageSpec;

    Default(
        ApplicationSpec.Dictionary scriptBody,
        FormRegistry formRegistry,
        Reporting reporting,
        LanguageSpec.ForJson languageSpec
    ) {
      super(formRegistry);
      this.scriptBody = requireNonNull(scriptBody);
      this.reporting = reporting;
      this.languageSpec = languageSpec;
    }

    @Override
    public Optional<Reporting> getReporting() {
      return Optional.ofNullable(reporting);
    }

    @Override
    public ApplicationSpec.Dictionary readRawBaseScript() {
      return scriptBody;
    }

    @Override
    public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
      return this.languageSpec;
    }
  }

  class Compat extends Base {
    private       Reporting  reporting = new Reporting("report.json", new File("."));
    private final Object     driverObject;
    private final Properties properties;
    final         CompatLoad loadAnnotation;

    public Compat(Class<?> driverClass, Properties properties) {
      super(createFormRegistry(createDriverObject(driverClass)));
      this.driverObject = createDriverObject(driverClass);
      this.properties = new Properties();
      this.properties.putAll(properties);
      this.loadAnnotation = ReflectionUtils.getAnnotation(driverClass, CompatLoad.class, CompatLoad.DEFAULT_INSTANCE);
    }

    private static FormRegistry createFormRegistry(Object driverObject) {
      return FormRegistry.getFormRegistry(driverObject);
    }

    public Compat(Class<?> driverClass, Properties properties, String scriptResourceName) {
      this(driverClass, properties);
      this.properties.put(this.loadAnnotation.scriptSystemPropertyKey(), scriptResourceName);
    }

    public Class getTestClass() {
      return this.driverObject.getClass();
    }

    public Optional<String> getScriptResourceName() {
      String work = this.properties.getProperty(
          getScriptResourceNameKey().orElseThrow(ScriptiveUnitException::noScriptResourceNameKeyWasGiven),
          this.loadAnnotation.script());
      return CompatLoad.SCRIPT_NOT_SPECIFIED.equals(work) ?
          Optional.empty() :
          Optional.of(work);
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
      return hostSpec()
          .readRawScript(
              getScriptResourceName().orElseThrow(() -> scriptNotSpecified(this)));
    }

    @Override
    public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
      return LanguageSpec.ForJson.create(formRegistry());
    }

    public Optional<String> getScriptResourceNameKey() {
      return Optional.of(loadAnnotation.scriptSystemPropertyKey());
    }
  }
}