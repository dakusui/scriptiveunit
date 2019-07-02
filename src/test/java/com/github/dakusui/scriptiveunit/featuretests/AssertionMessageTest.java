package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.junit.Test;
import org.junit.runner.notification.Failure;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asBoolean;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asObject;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.Crest.call;
import static com.github.dakusui.crest.Crest.substringAfterRegex;
import static com.github.dakusui.printables.Printables.isEmptyString;
import static org.junit.runner.JUnitCore.runClasses;

public class AssertionMessageTest {
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
  public void givenSimpleTestClass$whenRunTestClass$thenAssertionMessagesLooksGood() throws Throwable {
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
                        .after("matches").after("\\[false\\]")
                        .after("  output").after("\\[hello\\]")
                        .after("  \\'\\.\\*ELLO\\'").after("\\[[^]]+\\]")
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

  @Load(with = Simple.Loader.class)
  public static class Simple extends SimpleTestBase {
    public static class Loader extends SimpleTestBase.Loader {
      public Loader(Config config) {
        super(config);
      }

      @Override
      protected ApplicationSpec.Dictionary readScript(Config config, ApplicationSpec.Dictionary defaultValues) {
        return applicationSpec.deepMerge(
            dict(
                $("testOracles", array(
                    dict(
                        $("description", "shouldPass"),
                        $("when", array("format", "hello")),
                        $("then", array("matches", array("output"), ".*ell.*"))),
                    dict(
                        $("description", "shouldFail"),
                        $("when", array("format", "hello")),
                        $("then", array("matches", array("output"), ".*ELLO"))),
                    dict(
                        $("description", "shouldBeIgnored"),
                        $("given", array("not", array("always"))),
                        $("when", array("format", "hello")),
                        $("then", array("matches", array("output"), ".*Ell.*")))))),
            defaultValues);
      }
    }
  }
}
