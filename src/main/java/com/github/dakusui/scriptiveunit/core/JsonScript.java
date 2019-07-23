package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.HostSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.form.FormRegistry;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Optional;

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
  }

  class Default implements JsonScript {
    private final LanguageSpec.ForJson languageSpec;
    private final Reporting            reporting;
    private final String               scriptResourceName;

    Default(LanguageSpec.ForJson languageSpec, Reporting reporting, String scriptResourceName) {
      this.languageSpec = languageSpec;
      this.reporting = reporting;
      this.scriptResourceName = requireNonNull(scriptResourceName);
    }

    public String getScriptResourceName() {
      return this.scriptResourceName;
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

  }

  class FromDriverClass extends Default {
    private final Class<?> driverClass;
    private final String   scriptResourceNameKey;

    public FromDriverClass(Class<?> driverClass, String scriptResourceName) {
      super(Utils.createLanguageSpecFrom(Utils.createDriverObject(driverClass)),
          Reporting.create(),
          scriptResourceName
      );
      this.driverClass = requireNonNull(driverClass);
      this.scriptResourceNameKey = ScriptLoader.FromResourceSpecifiedBySystemProperty.getScriptResourceNameKey(driverClass);
    }

    public String getScriptResourceNameKey() {
      return scriptResourceNameKey;
    }

    public Class getTestClass() {
      return this.driverClass;
    }
  }

  enum Utils {
    ;

    public static JsonScript createScript(ApplicationSpec.Dictionary dictionary, Class<?> driverClass) {
      return createScript(dictionary, createLanguageSpecFromDriverClass(driverClass));
    }

    public static JsonScript createScript(ApplicationSpec.Dictionary dictionary, final LanguageSpec.ForJson languageSpecFromDriverClass) {
      return new Base() {
        private LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec = languageSpecFromDriverClass;
        Reporting reporting = Reporting.create();

        @Override
        public Optional<Reporting> getReporting() {
          return Optional.of(reporting);
        }

        @Override
        public ApplicationSpec.Dictionary readRawBaseScript() {
          return dictionary;
        }

        @Override
        public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
          return languageSpec;
        }
      };
    }

    public static Default createScriptFromResource(Class<?> driverClass, String scriptResourceName) {
      return createScriptFromResource(createLanguageSpecFromDriverClass(driverClass), scriptResourceName);
    }

    public static Default createScriptFromResource(
        LanguageSpec.ForJson languageSpecFromDriverClass,
        String scriptResourceName) {
      return createScriptFromResource(
          scriptResourceName,
          Reporting.create(),
          languageSpecFromDriverClass
      );
    }

    public static Default createScriptFromResource(
        String resourceName,
        Reporting reporting,
        LanguageSpec.ForJson languageSpec) {
      return new Default(
          languageSpec,
          reporting,
          resourceName);
    }

    private static LanguageSpec.ForJson createLanguageSpecFromDriverClass(Class<?> driverClass) {
      return LanguageSpec.ForJson.create(FormRegistry.createFormRegistry(createDriverObject(driverClass)));
    }

    private static LanguageSpec.ForJson createLanguageSpecFrom(Object driverObject) {
      return LanguageSpec.ForJson.create(FormRegistry.createFormRegistry(driverObject));
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