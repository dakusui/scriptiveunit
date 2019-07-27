package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.core.ScriptLoader.FromResourceSpecifiedBySystemProperty;
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
  static ApplicationSpec.Dictionary processScript(LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec) {
    ApplicationSpec.Dictionary rawScript = languageSpec.hostSpec().toApplicationDictionary(((ResourceStoreSpec.Impl) languageSpec.resourceStoreSpec()).mainNode());
    return languageSpec.createPreprocessor().preprocess(rawScript, languageSpec.resourceStoreSpec());
  }

  abstract class Base implements JsonScript {
    final         LanguageSpec.ForJson languageSpec;
    private final Reporting            reporting;
    private final ObjectNode           mainNode;

    protected Base(LanguageSpec.ForJson languageSpec, Reporting reporting, ObjectNode mainNode) {
      this.languageSpec = languageSpec;
      this.reporting = reporting;
      this.mainNode = mainNode;
    }

    @Override
    public Optional<Reporting> getReporting() {
      return Optional.ofNullable(reporting);
    }

    @Override
    public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
      return languageSpec;
    }

    @Override
    public ObjectNode mainNode() {
      return this.mainNode;
    }
  }

  class Default extends Base {
    private final String scriptResourceName;

    protected Default(LanguageSpec.ForJson languageSpec, Reporting reporting, String scriptResourceName, ObjectNode mainNode) {
      super(languageSpec, reporting, mainNode);
      this.scriptResourceName = requireNonNull(scriptResourceName);
    }

    public String getScriptResourceName() {
      return this.scriptResourceName;
    }
  }

  class FromDriverClass extends Default {
    private final Class<?> driverClass;
    private final String   scriptResourceNameKey;

    public FromDriverClass(Class<?> driverClass, String scriptResourceName, ObjectNode mainNode) {
      super(Utils.createLanguageSpecFrom(mainNode, FormRegistry.createFormRegistry(Utils.createDriverObject(driverClass))),
          Reporting.create(),
          scriptResourceName,
          mainNode
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
          createLanguageSpecFrom(mainNode, FormRegistry.createFormRegistry(createDriverObject(driverClass))),
          Reporting.create(), ((ResourceStoreSpec.Impl) createLanguageSpecFrom(mainNode, FormRegistry.createFormRegistry(createDriverObject(driverClass))).resourceStoreSpec()).mainNode()) {

        @Override
        public Optional<Reporting> getReporting() {
          return Optional.of(Reporting.create());
        }
      };
    }

    public static Default createScriptFromResource(Class<?> driverClass, String scriptResourceName) {
      return new Default(
          createLanguageSpecFrom(requireObjectNode(readJsonNodeFromStream(openResourceAsStream(scriptResourceName))), FormRegistry.createFormRegistry(createDriverObject(driverClass))),
          Reporting.create(),
          scriptResourceName, ((ResourceStoreSpec.Impl) createLanguageSpecFrom(requireObjectNode(readJsonNodeFromStream(openResourceAsStream(scriptResourceName))), FormRegistry.createFormRegistry(createDriverObject(driverClass))).resourceStoreSpec()).mainNode());
    }

    private static LanguageSpec.ForJson createLanguageSpecFrom(ObjectNode mainNode, FormRegistry formRegistry) {
      return LanguageSpec.ForJson.create(formRegistry, mainNode);
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