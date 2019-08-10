package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.core.ScriptLoader;
import com.github.dakusui.scriptiveunit.utils.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import static com.github.dakusui.scriptiveunit.utils.IoUtils.currentWorkingDirectory;

public abstract class SureSearchScriptLoaderBase extends ScriptLoader.Base {
  static abstract class ObjectNodeFactory implements JsonUtils.NodeFactory<ObjectNode> {
    @Override
    public abstract JsonNode create();

    ObjectNode createPrecisionTest(String s, ArrayNode evaluator) {
      return obj(
          $("description", $(s)),
          $("when", arr("issueRequest", arr("attr", "keyword"), 0, 10)),
          $("then",
              arr("verifyResponseWith",
                  arr("precisionBy",
                      evaluator,
                      arr("lambda", arr("eq", arr(0), 1.0))))));
    }

    ObjectNode addAsAfter(ObjectNode obj, ArrayNode after) {
      obj.put("after", after);
      return obj;
    }

    ObjectNode createNonEmptinessTest() {
      return addAsAfter(obj(
          $("description", $("Non-emptiness test")),
          $("when", arr("issueRequest", arr("attr", "keyword"), 0, 10)),
          $("then", arr("verifyResponseWith", arr("nonEmpty")))),
          arr("submit"));
    }

    ObjectNode createFactorSpace(String... keywords) {
      return obj($("factors",
          obj($("keyword", arr((Object[]) keywords)))));
    }
  }

  @Override
  public JsonScript load(Class<?> driverClass) {
    return JsonScript.Utils.createScript(
        driverClass,
        createObjectNodeFactory().get(),
        currentWorkingDirectory()
    );
  }

  abstract ObjectNodeFactory createObjectNodeFactory();
}
