package com.github.dakusui.scriptiveunit.unittests.issues;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.scriptiveunit.testassets.drivers.Simple;
import com.github.dakusui.scriptiveunit.testutils.JUnitResultMatcher;
import com.github.dakusui.scriptiveunit.testutils.TestDef;
import com.github.dakusui.scriptiveunit.testutils.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static com.github.dakusui.scriptiveunit.testutils.TestUtils.configureScriptNameSystemProperty;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JCUnit8.class)
public class BugfixesTest {
  @BeforeClass
  public static void beforeAll() {
    TestUtils.suppressStdOutErrIfRunUnderSurefire();
  }

  public enum TestItem implements TestDef<String, BugfixesTest, Result> {
    @SuppressWarnings("unused")ISSUE_1_NONTERMINATING_NUMBER_HANDLING {
      @Override
      public String getTestInput() {
        return "tests/issues/issue-2.json";
      }

      @Override
      public JUnitResultMatcher getOracle(BugfixesTest testObject) {
        return new JUnitResultMatcher.Builder()
            .withExpectedResult(true)
            .withExpectedRunCount(5)
            .withExpectedFailureCount(0)
            .withExpectedIgnoreCount(0)
            .build();
      }
    }
  }

  @ParameterSource
  public Parameter.Simple.Factory<TestItem> testItem() {
    return Parameter.Simple.Factory.of(singletonList(TestItem.ISSUE_1_NONTERMINATING_NUMBER_HANDLING));
  }


  @Test
  public void run(
      @From("testItem") TestItem testItem
  ) {
    configureScriptNameSystemProperty(testItem.getTestInput(), Simple.class);
    assertThat(JUnitCore.runClasses(Simple.class), testItem.getOracle(this));
  }

}
