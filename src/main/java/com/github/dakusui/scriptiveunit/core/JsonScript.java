package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.core.ScriptLoader.FromResourceSpecifiedBySystemProperty;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.form.FormRegistry;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.LanguageSpec;
import com.github.dakusui.scriptiveunit.model.lang.ResourceStoreSpec;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Optional;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapIfNecessary;
import static com.github.dakusui.scriptiveunit.utils.JsonUtils.readJsonNodeFromStream;
import static com.github.dakusui.scriptiveunit.utils.JsonUtils.requireObjectNode;
import static com.github.dakusui.scriptiveunit.utils.ReflectionUtils.openResourceAsStream;
import static java.util.Objects.requireNonNull;

public interface JsonScript extends Script<JsonNode, ObjectNode, ArrayNode, JsonNode> {
  @Override
  default ApplicationSpec.Dictionary readScriptResource(ResourceStoreSpec resourceStoreSpec, ObjectNode mainNode) {
    return staticReadScript(resourceStoreSpec, mainNode, this.languageSpec(), this.createPreprocessor());
  }

  static ApplicationSpec.Dictionary staticReadScript(ResourceStoreSpec resourceStoreSpec, ObjectNode script, LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec, Preprocessor preprocessor) {
    ApplicationSpec.Dictionary rawScript = languageSpec.hostSpec().toApplicationDictionary(script);
    return preprocessor.preprocess(rawScript, resourceStoreSpec);
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
      return base().getReporting();
    }

    @Override
    public ApplicationSpec.Dictionary readScriptResource(ResourceStoreSpec resourceStoreSpec, ObjectNode mainNode) {
      return base().readScriptResource(resourceStoreSpec, mainNode);
    }

    @Override
    public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
      return base().languageSpec();
    }

    @Override
    public String name() {
      return base().name();
    }

    @Override
    public Preprocessor createPreprocessor() {
      return base().createPreprocessor();
    }
  }

  abstract class Base implements JsonScript {
    final         LanguageSpec.ForJson languageSpec;
    private final Reporting            reporting;

    protected Base(LanguageSpec.ForJson languageSpec, Reporting reporting) {
      this.languageSpec = languageSpec;
      this.reporting = reporting;
    }

    @Override
    public Optional<Reporting> getReporting() {
      return Optional.ofNullable(reporting);
    }

    @Override
    public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
      return languageSpec;
    }
  }

  class Default extends Base {
    private final String scriptResourceName;

    Default(LanguageSpec.ForJson languageSpec, Reporting reporting, String scriptResourceName) {
      super(languageSpec, reporting);
      this.scriptResourceName = requireNonNull(scriptResourceName);
    }

    public String getScriptResourceName() {
      return this.scriptResourceName;
    }
  }

  class FromDriverClass extends Default {
    private final Class<?> driverClass;
    private final String   scriptResourceNameKey;

    public FromDriverClass(Class<?> driverClass, String scriptResourceName) {
      super(Utils.createLanguageSpecFrom(Utils.createDriverObject(driverClass),
          requireObjectNode(readJsonNodeFromStream(openResourceAsStream(scriptResourceName)))),
          Reporting.create(),
          scriptResourceName
      );
      this.driverClass = requireNonNull(driverClass);
      this.scriptResourceNameKey = FromResourceSpecifiedBySystemProperty.getScriptResourceNameKey(driverClass);
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

    public static JsonScript createScript(final Class<?> driverClass, final ObjectNode mainNode) {
      return new Base(
          createLanguageSpecFrom(createDriverObject(driverClass), mainNode),
          Reporting.create()) {

        @Override
        public Optional<Reporting> getReporting() {
          return Optional.of(Reporting.create());
        }
      };
    }

    public static Default createScriptFromResource(Class<?> driverClass, String scriptResourceName) {
      return new Default(
          createLanguageSpecFrom(createDriverObject(driverClass), requireObjectNode(readJsonNodeFromStream(openResourceAsStream(scriptResourceName)))),
          Reporting.create(),
          scriptResourceName);
    }

    private static LanguageSpec.ForJson createLanguageSpecFrom(Object driverObject, ObjectNode mainNode) {
      return LanguageSpec.ForJson.create(FormRegistry.createFormRegistry(driverObject), mainNode);
    }

    private static Object createDriverObject(Class<?> driverClass) {
      try {
        return driverClass.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw wrapIfNecessary(e);
      }
    }
  }
}