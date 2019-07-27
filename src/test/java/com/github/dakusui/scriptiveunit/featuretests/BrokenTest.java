package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.annotations.CompileWith;
import com.github.dakusui.scriptiveunit.annotations.LoadBy;
import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.core.ScriptLoader;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.utils.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static com.github.dakusui.crest.Crest.*;
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
      compiler = @CompileWith(Broken.Compiler.class))
  public static class Broken extends SimpleTestBase {
    public static class Loader extends ScriptLoader.Base {

      @Override
      public JsonScript load(Class<?> driverClass) {
        return JsonScript.Utils.createScript(driverClass, new JsonUtils.NodeFactory<ObjectNode>() {
          @Override
          public JsonNode create() {
            return obj($("testOracles", arr(
                obj(
                    $("when", arr("brokenForm")),
                    $("then", arr("matches", arr("output"), "bye"))
                ))));
          }
        }.get());
      }

    }

    public static class Compiler extends SimpleTestBase.Compiler {
      public Compiler() {
        super();
      }
    }
  }

}
