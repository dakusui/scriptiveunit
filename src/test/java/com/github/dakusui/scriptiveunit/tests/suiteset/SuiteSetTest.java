package com.github.dakusui.scriptiveunit.tests.suiteset;

import com.github.dakusui.jcunit8.factorspace.Parameter.Simple;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;

@RunWith(JCUnit8.class)
public class SuiteSetTest extends TestBase {
  public enum TestItem implements TestDef<Class, SuiteSetTest, Result> {
    @SuppressWarnings("unused")
    SUITESET_NORMAL_TEST {
      @Override
      public Matcher<Result> getOracle(SuiteSetTest testObject) {
        return new JUnitResultMatcher.Builder()
            .withExpectedResult(false)
            .withExpectedRunCount(4)
            .withExpectedFailureCount(2)
            .withExpectedIgnoreCount(0)
            .addFailureMatcher(1, new FailuresMatcher.EntryMatcher() {
              @Override
              protected boolean matchesFailure(Failure failure) {
                return failure.getTestHeader().contains("Suite 1");
              }

              @Override
              public void describeTo(Description description) {
                description.appendText("Expected to contain a string 'Suite 1'");
              }
            })
            .addFailureMatcher(1, new FailuresMatcher.EntryMatcher() {
              @Override
              protected boolean matchesFailure(Failure failure) {
                return failure.getTestHeader().contains("Suite 2");
              }

              @Override
              public void describeTo(Description description) {
                description.appendText("Expected to contain a string 'Suite 2'");
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

  @ParameterSource
  public Simple.Factory<TestItem> testItem() {
    return Simple.Factory.of(singletonList(TestItem.SUITESET_NORMAL_TEST));
  }

  @Test
  public void simple(
      @From("testItem") TestItem testItem
  ) {
    assertThat(JUnitCore.runClasses(testItem.getTestInput()), testItem.getOracle(this));
  }
}
