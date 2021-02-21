package com.github.dakusui.scriptiveunit.examples.searchengine.examples;

import com.github.dakusui.scriptiveunit.annotations.CompileWith;
import com.github.dakusui.scriptiveunit.annotations.LoadBy;
import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.codehaus.jackson.JsonNode;
import org.junit.runner.RunWith;

/**
 * A toy search engine named "SureSearch", which is for demonstrating "ScriptiveUnit"'s functionality.
 */
@RunScript(compiler = @CompileWith, loader = @LoadBy(BasicExample.Loader.class))
@RunWith(ScriptiveUnit.class)
@Expect(passing = 6, failing = 10, ignored = 0)
public class BasicExample extends SureSearchExampleBase {
  public static class Loader extends SureSearchScriptLoaderBase {
    @Override
    ObjectNodeFactory createObjectNodeFactory() {
      return new ObjectNodeFactory() {
        @Override
        public JsonNode create() {
          return obj(
              $("factorSpace", createFactorSpace()),
              $("define", createUserForms()),
              $("testOracles", arr(
                  createNonEmptinessTest(),
                  createDcgTest("A DCG test by pre-defined lambda", 3,
                      arr("evaluatorByLambda", arr("findWord"))),
                  createNDcgTest("An nDCG test by pre-defined lambda", 3,
                      arr("evaluatorByLambda", arr("findWord"))),
                  createPrecisionTest("A precision test by pre-defined lambda",
                      arr("evaluatorByLambda", arr("findWord"))),
                  createPrecisionTest("A precision test by lambda",
                      arr("evaluatorByLambda", arr("lambda",
                          arr("find",
                              arr("docAttr", arr(0), "description"),
                              arr("format", "\\b%s\\b", arr("attr", "keyword")))))),
                  createPrecisionTest("A precision test by known relevant id set",
                      arr("evaluatorByKnownRelevantDocIds", "0", "5")),
                  createPrecisionTest("A precision test by known irrelevant id set", arr("evaluatorByKnownIrrelevantDocIds", "2", "3", "4")),
                  createPrecisionTest("A precision test by default evaluator", arr("defaultEvaluator"))
              )));
        }
      };
    }
  }
}
