package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.annotations.LoadBy;
import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.core.LanguageSpec;
import com.github.dakusui.scriptiveunit.core.Reporting;
import com.github.dakusui.scriptiveunit.core.ScriptLoader;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

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

  @RunScript(
      loader = @LoadBy(Broken.Loader.class),
      compileWith = Broken.Compiler.class)
  public static class Broken extends SimpleTestBase {
    public static class Loader extends ScriptLoader.Base {

      @Override
      public JsonScript load(Class<?> driverClass) {
        return new JsonScript.Base() {
          private LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec = JsonScript.Default.createLanguageSpecFromDriverClass(driverClass);
          Reporting reporting = Reporting.create();

          @Override
          public Optional<Reporting> getReporting() {
            return Optional.of(reporting);
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

          @Override
          public LanguageSpec<JsonNode, ObjectNode, ArrayNode, JsonNode> languageSpec() {
            return languageSpec;
          }
        };
      }
    }

    public static class Compiler extends SimpleTestBase.Compiler {
      public Compiler() {
        super();
      }
    }
  }
}
