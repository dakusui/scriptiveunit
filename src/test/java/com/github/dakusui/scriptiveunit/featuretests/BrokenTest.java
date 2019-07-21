package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asObject;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.Crest.call;
import static com.github.dakusui.scriptiveunit.testutils.TestUtils.runClasses;

public class BrokenTest {
  @Test(expected = ScriptiveUnitException.class)
  public void test() throws Throwable {
    try {
      throw runClasses(BrokenTest.Broken.class).getFailures().get(0).getException();
    } catch (ScriptiveUnitException e) {
      assertThat(
          e.getCause(),
          allOf(
              asObject().isInstanceOf(InvocationTargetException.class)
                  .$(),
              asObject(call("getCause").$()).isInstanceOf(RuntimeException.class).$(),
              asString(call("getCause").andThen("getMessage").$())
                  .equalTo("brokenForm")
                  .$()
          )
      );
      throw e;
    }
  }

  @RunScript(compileWith = Broken.Compiler.class)
  public static class Broken extends SimpleTestBase {
    public static class Compiler extends SimpleTestBase.Compiler {
      public Compiler() {
        super();
      }


      JsonScript createScript(JsonScript script) {
        return new JsonScript.Delegating(script) {
          @Override
          public ApplicationSpec.Dictionary readScriptResource() {
            return createPreprocessor().preprocess(readRawBaseScript());
          }

          @Override
          public ApplicationSpec.Dictionary readRawBaseScript() {
            return new SyntaxSugar() {
              ApplicationSpec.Dictionary create() {
                return dict(
                    $("testOracles", array(
                        dict(
                            $("when", array("brokenForm")),
                            $("then", array("matches", array("output"), "bye"))
                        ))));
              }
            }.create();
          }
        };
      }
    }
  }
}
