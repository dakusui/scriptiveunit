package com.github.dakusui.scriptunit.tests.bugfixes;

import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.drivers.Simple;
import com.github.dakusui.scriptunit.testutils.JUnitResultMatcher;
import com.github.dakusui.scriptunit.testutils.TestDef;
import com.github.dakusui.scriptunit.testutils.TestUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JCUnit.class)
public class BugfixesTest {

  public enum TestItem implements TestDef<String, BugfixesTest, Result> {
    @ReflectivelyReferenced ISSUE_1_NONTERMINATING_NUMBER_HANDLING {
      @Override
      public String getTestInput() {
        return "tests/bugfixes/issue-2.json";
      }

      @Override
      public JUnitResultMatcher getOracle(BugfixesTest testObject) {
        return new JUnitResultMatcher.Impl(true, 5, 0, 0);
      }
    };
  }

  @ReflectivelyReferenced
  @FactorField
  public TestItem testItem;

  @Test
  public void run() {
    TestUtils.configureScriptNameSystemProperty(testItem.getTestInput(), Simple.class);
    assertThat(JUnitCore.runClasses(Simple.class), testItem.getOracle(this));
  }

}
