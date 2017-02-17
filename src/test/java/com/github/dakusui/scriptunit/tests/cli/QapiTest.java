package com.github.dakusui.scriptunit.tests.cli;

import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.drivers.Qapi;
import com.github.dakusui.scriptunit.drivers.Simple;
import com.github.dakusui.scriptunit.testutils.JUnitResultMatcher;
import com.github.dakusui.scriptunit.testutils.TestBase;
import com.github.dakusui.scriptunit.testutils.TestDef;
import com.github.dakusui.scriptunit.testutils.TestUtils;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JCUnit.class)
public class QapiTest extends TestBase {
  public enum TestItem implements TestDef<String, QapiTest, Result> {
    @ReflectivelyReferenced DEFAULT_VALUES("tests/regular/defaultValues.json", false, 18, 6, 0),
    @ReflectivelyReferenced STANDARD_VALUES("tests/regular/qapi.json", false, 36, 9, 0);

    final private String  scriptResourceName;
    final private boolean wasSuccessful;
    final private int     expectedRunCount;
    final private Integer expectedIgnoreCount;
    final private int     expectedFailureCount;

    TestItem(String scriptResourceName, boolean wasSuccessful, int expectedRunCount, int expectedFailureCount, Integer expectedIgnoreCount) {
      this.scriptResourceName = scriptResourceName;
      this.wasSuccessful = wasSuccessful;
      this.expectedRunCount = expectedRunCount;
      this.expectedFailureCount = expectedFailureCount;
      this.expectedIgnoreCount = expectedIgnoreCount;
    }

    @Override
    public String getTestInput() {
      return this.scriptResourceName;
    }

    @Override
    public Matcher<Result> getOracle(QapiTest testObject) {
      return new JUnitResultMatcher.Impl(this.wasSuccessful, this.expectedRunCount, this.expectedFailureCount, this.expectedIgnoreCount);
    }
  }

  @ReflectivelyReferenced
  @FactorField
  public TestItem testItem;

  @Test
  public void runTest() {
    TestUtils.configureScriptNameSystemProperty(testItem.getTestInput(), Simple.class);
    assertThat(JUnitCore.runClasses(Qapi.class), testItem.getOracle(this));
  }
}
