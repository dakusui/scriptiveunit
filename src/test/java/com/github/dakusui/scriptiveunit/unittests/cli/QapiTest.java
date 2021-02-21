package com.github.dakusui.scriptiveunit.unittests.cli;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.scriptiveunit.examples.qapi.Qapi;
import com.github.dakusui.scriptiveunit.testassets.drivers.Simple;
import com.github.dakusui.scriptiveunit.testutils.JUnitResultMatcher;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import com.github.dakusui.scriptiveunit.testutils.TestDef;
import com.github.dakusui.scriptiveunit.testutils.TestUtils;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static com.github.dakusui.scriptiveunit.testutils.TestUtils.runClasses;
import static com.github.dakusui.scriptiveunit.unittests.cli.QapiTest.TestItem.DEFAULT_VALUES;
import static com.github.dakusui.scriptiveunit.unittests.cli.QapiTest.TestItem.STANDARD_VALUES;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JCUnit8.class)
public class QapiTest extends TestBase {
  public enum TestItem implements TestDef<String, QapiTest, Result> {
    @SuppressWarnings("unused")DEFAULT_VALUES("tests/regular/defaultValues.json", false, 18, 6, 6),
    @SuppressWarnings("unused")STANDARD_VALUES("examples/qapi.json", false, 27, 9, 9);

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
      return new JUnitResultMatcher.Builder()
          .withExpectedResult(this.wasSuccessful)
          .withExpectedRunCount(this.expectedRunCount)
          .withExpectedFailureCount(this.expectedFailureCount)
          .withExpectedIgnoreCount(this.expectedIgnoreCount)
          .build();
    }
  }

  @SuppressWarnings("unused")
  @ParameterSource
  public Parameter.Simple.Factory<TestItem> testItem() {
    return Parameter.Simple.Factory.of(asList(DEFAULT_VALUES, STANDARD_VALUES));
  }

  @Test
  public void runTest(
      @From("testItem") TestItem testItem
  ) {
    System.out.println(testItem.getTestInput());
    TestUtils.configureScriptNameSystemProperty(testItem.getTestInput(), Simple.class);
    assertThat(runClasses(Qapi.class), testItem.getOracle(this));
  }
}
