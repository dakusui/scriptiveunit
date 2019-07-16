package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Config;
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

  @Load(with = Broken.Loader.class)
  public static class Broken extends SimpleTestBase {
    public static class Loader extends SimpleTestBase.Loader {
      public Loader(Config config) {
        super(new Config.Delegating(config) {
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
        });
      }
    }
  }
}
