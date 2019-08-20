package com.github.dakusui.scriptiveunit.examples.searchengine.examples;

import com.github.dakusui.scriptiveunit.annotations.CompileWith;
import com.github.dakusui.scriptiveunit.annotations.LoadBy;
import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.runner.RunWith;

@RunScript(compiler = @CompileWith, loader = @LoadBy(KeywordFactorExample.Loader.class))
@RunWith(ScriptiveUnit.class)
@Expect(passing = 1, failing = 1, ignored = 0)
public class KeywordFactorExample extends SureSearchExampleBase {
  public static class Loader extends SureSearchScriptLoaderBase {
    @Override
    ObjectNodeFactory createObjectNodeFactory() {
      return new ObjectNodeFactory() {
        @Override
        public ObjectNode create() {
          return obj(
              $("factorSpace", createFactorSpace()),
              $("define", createUserForms()),
              $("testOracles", arr(
                  createPrecisionTest(
                      "A precision test by default evaluator",
                      arr("defaultEvaluator")))));
        }
      };
    }
  }
}
