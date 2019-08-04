package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.annotations.CompileWith;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.LoadBy;
import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.core.ScriptLoader;
import com.github.dakusui.scriptiveunit.libs.Core;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.extras.SearchEngineLib;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.Dictionary;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.utils.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.runner.RunWith;

import static com.github.dakusui.scriptiveunit.utils.IoUtils.currentWorkingDirectory;

/**
 * A search engine named "SureSearch", which is for explaining "ScriptiveUnit"'s functionality.
 */
@RunScript(compiler = @CompileWith, loader = @LoadBy(SureSearchTest.Loader.class))
@RunWith(ScriptiveUnit.class)
public class SureSearchTest {
  public static class Loader extends ScriptLoader.Base {
    @Override
    public JsonScript load(Class<?> driverClass) {
      return JsonScript.Utils.createScript(driverClass, new JsonUtils.NodeFactory<ObjectNode>() {
            @Override
            public JsonNode create() {
              return obj(
                  $("testOracles", arr(
                      obj(
                          $("description", $("A precision test by known relevant id set evaluator")),
                          $("when", arr("issueRequest", "apple", 0, 10)),
                          $("then",
                              arr("verifyResponseWith",
                                  arr("precisionBy",
                                      arr("evaluatorByKnownRelevantDocIds", "0", "5"),
                                      arr("lambda", arr("eq", arr(0), 1.0)))))),
                      obj(
                          $("description", $("A precision test by default evaluator")),
                          $("when", arr("issueRequest", "apple", 0, 10)),
                          $("then",
                              arr("verifyResponseWith",
                                  arr("precisionBy",
                                      arr("defaultEvaluator"),
                                      arr("lambda", arr("eq", arr(0), 1.0)))))),
                      obj(
                          $("description", $("Non-emptiness test")),
                          $("when", arr("issueRequest", "apple", 0, 10)),
                          $("then", arr("verifyResponseWith", arr("nonEmpty"))))
                  )));
            }
          }.get(),
          currentWorkingDirectory()
      );
    }
  }

  @Import
  public final Core core = new Core();

  @Import
  public final Predicates predicates = new Predicates();

  @Import
  public final SearchEngineLib<SureSearchRequest, SureSearchResponse, Dictionary> searchEngineLib =
      new SearchEngineLib<>(
          new SureSearch(SureSearchDocSet.DEFAULT.docs()),
          new SureSearchResultEvaluator(SureSearchDocSet.DEFAULT));

}
