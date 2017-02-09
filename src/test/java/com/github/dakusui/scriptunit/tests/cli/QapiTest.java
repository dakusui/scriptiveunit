package com.github.dakusui.scriptunit.tests.cli;

import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.core.Config;
import com.github.dakusui.scriptunit.drivers.Qapi;
import com.github.dakusui.scriptunit.testutils.TestBase;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JCUnit.class)
public class QapiTest extends TestBase {
  @ReflectivelyReferenced
  @FactorField(stringLevels = { "tests/regular/qapi.json", "tests/regular/defaults.json" })
  public String resourceName;

  @Test
  public void runWithNormalExample() {
    String scriptSystemPropertyKey = Config.create(Qapi.class, System.getProperties()).getScriptSystemPropertyKey();
    System.setProperty(scriptSystemPropertyKey, resourceName);
    Result result = JUnitCore.runClasses(Qapi.class);
    assertThat(result.wasSuccessful(), equalTo(false));
    assertThat(result.getRunCount(), equalTo(27));
    assertThat(result.getFailureCount(), equalTo(9));
    assertThat(result.getIgnoreCount(), equalTo(0));
  }
}
