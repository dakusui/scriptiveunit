package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static org.junit.runner.JUnitCore.runClasses;

public class AssertionMessageTest {
  @Test
  public void givenSimpleTestClass$whenRunTestClass$thenExpectedResult() throws Throwable {
    ResultExpectation expectation = buildResultExpectation(SimpleTest.class);
    assertThat(
        runClasses(SimpleTest.class),
        allOf(
            asBoolean("wasSuccessful").equalTo(false).$(),
            asInteger("getRunCount").equalTo(expectation.getRunCount()).$(),
            asInteger("getFailureCount").equalTo(expectation.getFailureRunCount()).$(),
            asInteger("getIgnoreCount").equalTo(expectation.getIgnoreCount()).$()));
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

  @Load(with = SimpleTest.Loader.class)
  public static class SimpleTest extends SimpleTestBase {
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
