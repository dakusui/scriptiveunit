package com.github.dakusui.scriptiveunit.examples.searchengine.examples;

import com.github.dakusui.scriptiveunit.annotations.CompileWith;
import com.github.dakusui.scriptiveunit.annotations.LoadBy;
import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.runner.RunWith;

@RunScript(compiler = @CompileWith, loader = @LoadBy(PracticalExample.Loader.class))
@RunWith(ScriptiveUnit.class)
public class PracticalExample extends SureSearchExampleBase {
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
                  createDetectedNoiseRateTest("A detected noise-rate test by 'pineapple'",
                      arr("evaluatorByLambda", arr("findPineapple"))),
                  createDetectedNoiseRateTest("A detected noise-rate test by 'apple.com'",
                      arr("evaluatorByLambda", arr("findAppleDotCom")))
              )));
        }

        @Override
        ObjectNode createFactorSpace() {
          return createFactorSpace("apple");
        }

        @Override
        ObjectNode createUserForms() {
          return obj(
              $("findWord", arr("lambda",
                  arr("find",
                      arr("docAttr", arr(0), "description"),
                      arr("format", "\\b%s\\b", arr("attr", "keyword"))))),
              $("findAppleDotCom", arr("lambda",
                  arr("find",
                      arr("docAttr", arr(0), "description"),
                      "\\bapple\\.com\\b"))),
              $("findPineapple", arr("lambda",
                  arr("find",
                      arr("docAttr", arr(0), "description"),
                      "\\bpineapple\\b"))));
        }
      };
    }
  }
}
