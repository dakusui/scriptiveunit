package com.github.dakusui.scriptiveunit.tests.suiteset;

import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.testutils.FailuresMatcher;
import com.github.dakusui.scriptiveunit.testutils.JUnitResultMatcher;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import com.github.dakusui.scriptiveunit.testutils.TestDef;
import com.github.dakusui.scriptiveunit.testutils.drivers.ExampleSuiteSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import static org.junit.Assert.assertThat;

@RunWith(JCUnit.class)
public class SuiteSetTest extends TestBase {
  public enum TestItem implements TestDef<Class, SuiteSetTest, Result> {
    @ReflectivelyReferenced
    SUITESET_NORMAL_TEST {
      @Override
      public Matcher<Result> getOracle(SuiteSetTest testObject) {
        return new JUnitResultMatcher.Builder()
            .withExpectedResult(false)
            .withExpectedRunCount(4)
            .withExpectedFailureCount(2)
            .withExpectedIgnoreCount(0)
            .addFailureMatcher(0, new FailuresMatcher.EntryMatcher() {
              @Override
              protected boolean matchesFailure(Failure failure) {
                return failure.getTestHeader().contains("Suite 1");
              }

              @Override
              public void describeTo(Description description) {
                description.appendText("Expected to contain a string 'Suite'");
              }
            })
            .addFailureMatcher(1, new FailuresMatcher.EntryMatcher() {
              @Override
              protected boolean matchesFailure(Failure failure) {
                return failure.getTestHeader().contains("Suite 2");
              }

              @Override
              public void describeTo(Description description) {
                description.appendText("Expected to contain a string 'Suite'");
              }
            })
            .build();
      }

      @Override
      public Class getTestInput() {
        return ExampleSuiteSet.class;
      }
    }
  }

  @ReflectivelyReferenced
  @FactorField
  public TestItem testItem;

  @Test
  public void simple() {
    assertThat(JUnitCore.runClasses(testItem.getTestInput()), testItem.getOracle(this));
  }
}
