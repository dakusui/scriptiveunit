package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.annotations.CompileWith;
import com.github.dakusui.scriptiveunit.annotations.LoadBy;
import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.codehaus.jackson.JsonNode;
import org.junit.runner.RunWith;

/**
 * A toy search engine named "SureSearch", which is for demonstrating "ScriptiveUnit"'s functionality.
 */
@RunScript(compiler = @CompileWith, loader = @LoadBy(SureSearchExample.Loader.class))
@RunWith(ScriptiveUnit.class)
public class SureSearchExample extends SureSearchTestBase {
  static class Loader extends SureSearchScriptLoaderBase {

    @Override
    ObjectNodeFactory createObjectNodeFactory() {
      return new ObjectNodeFactory() {
        @Override
        public JsonNode create() {
          return obj(
              $("factorSpace", createFactorSpace("apple", "orange")),
              $("testOracles", arr(
                  createNonEmptinessTest(),
                  addAsAfter(
                      createPrecisionTest("A precision test by lambda",
                          arr("evaluatorByLambda",
                              arr("lambda",
                                  arr("find",
                                      arr("docAttr", arr(0), "description"),
                                      " +apple")))),
                      arr("submit")),
                  addAsAfter(
                      createPrecisionTest("A precision test by known relevant id set",
                          arr("evaluatorByKnownRelevantDocIds", "0", "5")),
                      arr("submit")),
                  addAsAfter(
                      createPrecisionTest("A precision test by known irrelevant id set", arr("evaluatorByKnownIrrelevantDocIds", "2", "3", "4")),
                      arr("submit")),
                  addAsAfter(
                      createPrecisionTest("A precision test by default evaluator", arr("defaultEvaluator")),
                      arr("submit"))
              )));
        }
      };
    }
  }
}
