package com.github.dakusui.scriptiveunit.modeltests;

import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.model.lang.LanguageSpec;
import com.github.dakusui.scriptiveunit.core.Reporting;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.form.FormRegistry;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Optional;

public class UtScript implements JsonScript {
  private final LanguageSpec.ForJson languageSpec;

  private UtScript(LanguageSpec.ForJson languageSpec) {
    this.languageSpec = languageSpec;
  }

  @Override
  public Optional<Reporting> getReporting() {
    return Optional.empty();
  }

  @Override
  public ApplicationSpec.Dictionary readRawBaseScript() {
    throw new UnsupportedOperationException();
  }

  @Override
  public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
    return languageSpec;
  }

  static JsonScript create(FormRegistry formRegistry) {
    return new UtScript(LanguageSpec.ForJson.create(formRegistry));
  }
}
