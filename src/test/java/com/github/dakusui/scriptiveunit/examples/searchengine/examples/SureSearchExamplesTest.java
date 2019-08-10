package com.github.dakusui.scriptiveunit.examples.searchengine.examples;

import com.github.dakusui.scriptiveunit.testutils.TestBase;
import com.github.dakusui.scriptiveunit.testutils.TestUtils;
import org.junit.Test;
import org.junit.runner.Result;

import static com.github.dakusui.crest.Crest.*;
import static java.util.Objects.requireNonNull;

public class SureSearchExamplesTest extends TestBase {
  @Test
  public void testBasicExample() {
    runTestClassAndVerifyResult(BasicExample.class);
  }

  @Test
  public void testKeywordExample() {
    runTestClassAndVerifyResult(KeywordFactorExample.class);
  }

  @Test
  public void testReportSubmissionExample() {
    runTestClassAndVerifyResult(ReportSubmissionExample.class);
  }

  @Test
  public void testUserDefinedFormExample() {
    runTestClassAndVerifyResult(UserDefinedFormExample.class);
  }


  private void runTestClassAndVerifyResult(Class<?> testClass) {
    assertResultWith(TestUtils.runClasses(testClass), expect(testClass));
  }

  private Expect expect(Class<?> testClass) {
    return requireNonNull(testClass.getAnnotation(Expect.class));
  }

  private void assertResultWith(Result result, Expect expect) {
    System.out.println(result.wasSuccessful());
    System.out.println(result.getRunCount());
    System.out.println(result.getFailureCount());
    System.out.println(result.getIgnoreCount());

    assertThat(
        result,
        allOf(
            asBoolean("wasSuccessful").equalTo(expect.failing() == 0).$(),
            asInteger("getRunCount").equalTo(expect.passing() + expect.failing() + expect.ignored()).$(),
            asInteger("getFailureCount").equalTo(expect.failing()).$(),
            asInteger("getIgnoreCount").equalTo(expect.ignored()).$()
        )
    );
  }
}
