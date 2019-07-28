package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.annotations.CompileWith;
import com.github.dakusui.scriptiveunit.annotations.LoadBy;
import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.core.ScriptLoader;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import com.github.dakusui.scriptiveunit.utils.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.notification.Failure;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.printables.Printables.isEmptyString;
import static com.github.dakusui.scriptiveunit.testutils.TestUtils.runClasses;
import static com.github.dakusui.scriptiveunit.utils.IoUtils.currentWorkingDirectory;

public class AssertionMessageTest extends TestBase {

  @Test
  public void givenSimpleTestClass$whenRunTestClass$thenExpectedResult() throws Throwable {
    ResultExpectation expectation = buildResultExpectation(Simple.class);
    assertThat(
        runClasses(Simple.class),
        allOf(
            asBoolean("wasSuccessful").equalTo(false).$(),
            asInteger("getRunCount").equalTo(expectation.getRunCount()).$(),
            asInteger("getFailureCount").equalTo(expectation.getFailureRunCount()).$(),
            asInteger("getIgnoreCount").equalTo(expectation.getIgnoreCount()).$()));
  }

  @Test
  public void givenSimpleTestClass$whenRunTestClass$thenAssertionMessagesLooksGood() {
    Failure failure = runClasses(Simple.class).getFailures().get(0);
    System.out.println("header=" + failure.getTestHeader());
    System.out.println("description=" + failure.getDescription());
    System.out.println("exception=" + failure.getException().getClass());
    System.out.println("message=" + failure.getMessage());
    System.out.println("toString=" + failure.toString());
    assertThat(
        failure,
        allOf(
            asString("getTestHeader").containsString("shouldFail").$(),
            asObject("getException").isInstanceOf(AssertionError.class).$(),
            asString(call("getMessage").$())
                .check(
                    substringAfterRegex("Expected:")
                        .after("output:\\<hello\\>")
                        .after("criterion:\\<\\(matches \\(output\\) .+\\)\\>")
                        .after("     but: output \\<hello\\> did not satisfy it")
                        .after("matches").after("false")
                        .after("  output").after("hello")
                        .after("  const:'\\.\\*ELLO'").after(":\\.\\*ELLO")
                        .$(),
                    isEmptyString().negate()
                ).$()
        ));
  }

  @SuppressWarnings("SameParameterValue")
  private static ResultExpectation buildResultExpectation(Class<?> klass) throws Throwable {
    ResultExpectation resultExpectation = new ResultExpectation();
    new ScriptiveUnit(klass).getTestSuiteDescriptor().getTestOracles()
        .stream()
        .map(o -> o.getDescription().orElseThrow(RuntimeException::new))
        .peek((String d) -> resultExpectation.runCount++)
        .peek((String d) -> resultExpectation.failureCount += d.contains("shouldFail") ? 1 : 0)
        .peek((String d) -> resultExpectation.ignoreCount += d.contains("shouldBeIgnored") ? 1 : 0)
        .forEach(d -> {
        });
    return resultExpectation;
  }

  public static class ResultExpectation {
    int runCount;
    int ignoreCount;
    int failureCount;

    int getRunCount() {
      return runCount;
    }

    int getFailureRunCount() {
      return failureCount;
    }

    int getIgnoreCount() {
      return ignoreCount;
    }

    @Override
    public String toString() {
      return String.format("runCount=<%s>, failureCount=<%s>, ignoreCount=<%s>%n", runCount, failureCount, ignoreCount);
    }
  }

  @RunScript(
      loader = @LoadBy(Simple.Loader.class),
      compiler = @CompileWith(Simple.Compiler.class))
  public static class Simple extends SimpleTestBase {
    public static class Loader extends ScriptLoader.Base {
      @Override
      public JsonScript load(Class<?> driverClass) {
        return JsonScript.Utils.createScript(driverClass, new JsonUtils.NodeFactory<ObjectNode>() {
              @Override
              public JsonNode create() {
                return obj(
                    $("testOracles", arr(
                        obj(
                            $("description", $("shouldPass")),
                            $("when", arr("format", "hello")),
                            $("then", arr("matches", arr("output"), ".*ell.*"))),
                        obj(
                            $("description", $("shouldFail")),
                            $("when", arr("format", "hello")),
                            $("then", arr("matches", arr("output"), ".*ELLO"))),
                        obj(
                            $("description", $("shouldBeIgnored")),
                            $("given", arr("not", arr("always"))),
                            $("when", arr("format", "hello")),
                            $("then", arr("matches", arr("output"), ".*Ell.*"))))));
              }
            }.get(),
            currentWorkingDirectory()
        );
      }
    }

    public static class Compiler extends SimpleTestBase.Compiler implements ApplicationSpec.Dictionary.Factory {
      public Compiler() {
        super();
      }

    }
  }
}
