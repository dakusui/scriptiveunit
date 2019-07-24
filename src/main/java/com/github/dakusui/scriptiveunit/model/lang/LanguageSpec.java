package com.github.dakusui.scriptiveunit.model.lang;

import com.github.dakusui.scriptiveunit.model.form.FormRegistry;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import static java.util.Objects.requireNonNull;

public interface LanguageSpec<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
  HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec();

  ApplicationSpec applicationSpec();

  FormRegistry formRegistry();

  interface ForJson extends LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> {
    static ForJson create(FormRegistry formRegistry) {
      requireNonNull(formRegistry);
      return new ForJson() {
        HostSpec.Json hostSpec = new HostSpec.Json();
        ApplicationSpec applicationSpec = new ApplicationSpec.Standard();

        @Override
        public HostSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> hostSpec() {
          return hostSpec;
        }

        @Override
        public ApplicationSpec applicationSpec() {
          return applicationSpec;
        }

        @Override
        public FormRegistry formRegistry() {
          return formRegistry;
        }
      };
    }
  }
}
