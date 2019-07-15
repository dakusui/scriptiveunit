package com.github.dakusui.scriptiveunit.unittests.negative;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import com.github.dakusui.scriptiveunit.examples.Qapi;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BrokenScriptsTestExample extends TestBase {
  @Test
  public void runBrokenScript() {
    use("tests/negative/01-malformed-script/script.json");
    Result result = JUnitCore.runClasses(Qapi.class);
    assertThat(
        asStrings(result),
        equalTo(resultStrings(
            false,
            1,
            1,
            0
        ))
    );
    assertThat(
        asStrings(result.getFailures().get(0)),
        equalTo(failureStrings("", "", "", ""))
    );
  }

  private List<String> asStrings(Result result) {
    return resultStrings(
        result.wasSuccessful(),
        result.getRunCount(),
        result.getFailureCount(),
        result.getIgnoreCount());
  }

  private List<String> resultStrings(boolean wasSuccessful, int runCount, int failureCount, int ignoreCount) {
    return asList(
        format("wasSuccessful:%s%n", wasSuccessful),
        format("runCount:%s%n", runCount),
        format("failureCount:%s%n", failureCount),
        format("ignoreCount:%s%n", ignoreCount)
    );
  }

  private List<String> asStrings(Failure failure) {
    Description description = failure.getDescription();
    return failureStrings(description.getDisplayName(), description.getClassName(), description.getMethodName(), failure.getMessage());
  }

  private List<String> failureStrings(String displayName, String className, String methodName, String message) {
    return asList(
        format("name:%s%n", displayName),
        format("class:%s%n", className),
        format("method:%s%n", methodName),
        format("message:%s%n", message)
    );

  }


  private void use(String s) {
    String scriptSystemPropertyKey = new Config.Builder(Qapi.class, System.getProperties())
        .build()
        .getScriptResourceNameKey()
        .orElseThrow(ScriptiveUnitException::noScriptResourceNameKeyWasGiven);
    System.setProperty(scriptSystemPropertyKey, s);
  }
}
