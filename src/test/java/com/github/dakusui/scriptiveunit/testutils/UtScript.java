package com.github.dakusui.scriptiveunit.testutils;

import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.core.Reporting;
import com.github.dakusui.scriptiveunit.model.form.FormRegistry;
import com.github.dakusui.scriptiveunit.model.lang.LanguageSpec;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Optional;

import static com.github.dakusui.scriptiveunit.utils.IoUtils.currentWorkingDirectory;

public class UtScript implements JsonScript {
  private final LanguageSpec.ForJson languageSpec;
  private final ObjectNode           mainNode;

  private UtScript(LanguageSpec.ForJson languageSpec, ObjectNode mainNode) {
    this.languageSpec = languageSpec;
    this.mainNode = mainNode;
  }

  @Override
  public Optional<Reporting> getReporting() {
    return Optional.empty();
  }

  @Override
  public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
    return languageSpec;
  }

  @Override
  public ObjectNode mainNode() {
    return mainNode;
  }

  public static JsonScript create(FormRegistry formRegistry, ObjectNode mainNode) {
    return new UtScript(LanguageSpec.ForJson.create(formRegistry, currentWorkingDirectory()), mainNode);
  }
}
