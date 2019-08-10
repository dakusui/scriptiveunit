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
@Expect(passing = 4, failing = 8, ignored = 0)
public class BasicExample extends SureSearchExampleBase {
  public static class Loader extends SureSearchScriptLoaderBase {
    @Override
    ObjectNodeFactory createObjectNodeFactory() {
      return new ObjectNodeFactory() {
        @Override
        public JsonNode create() {
          return obj(
              $("factorSpace", createFactorSpace("apple", "orange")),
              $("define", createUserForms()),
              $("testOracles", arr(
                  createNonEmptinessTest(),
                  createPrecisionTest("A precision test by pre-defined lambda",
                      arr("evaluatorByLambda", arr("findApple"))),
                  addAsAfter(
                      createPrecisionTest("A precision test by lambda",
                          arr("evaluatorByLambda", arr("lambda",
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
