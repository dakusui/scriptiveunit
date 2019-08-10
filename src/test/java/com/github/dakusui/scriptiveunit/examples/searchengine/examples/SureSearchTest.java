package com.github.dakusui.scriptiveunit.examples.searchengine.examples;

import com.github.dakusui.scriptiveunit.testutils.TestBase;
import com.github.dakusui.scriptiveunit.testutils.TestUtils;
import org.junit.Test;
import org.junit.runner.Result;

import static com.github.dakusui.crest.Crest.*;
import static java.util.Objects.requireNonNull;

public class SureSearchTest extends TestBase {
  @Test
  public void test() {
    runTestClassAndVerifyResult(UserDefinedFormExample.class);
  }

  public void runTestClassAndVerifyResult(Class<?> testClass) {
    assertResultWith(TestUtils.runClasses(testClass), expect(testClass));
  }

  Expect expect(Class<?> testClass) {
    return requireNonNull(testClass.getAnnotation(Expect.class));
  }

  void assertResultWith(Result result, Expect expect) {
    System.out.println(result.wasSuccessful());
    System.out.println(result.getFailureCount());
    System.out.println(result.getIgnoreCount());

    assertThat(
        result,
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getFailureCount").$(),
            asInteger("getIgnoreCount").$()
        )
    );
  }
}
