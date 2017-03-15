package com.github.dakusui.scriptiveunit.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class UtilsTest {
  @Test
  public void whenAllTypesAnnotatedWith$thenThisClassIsFound() {
    assertTrue(
        Utils.allTypesAnnotatedWith("com.github.dakusui.scriptiveunit", RunWith.class)
            .anyMatch(UtilsTest.class::equals)
    );
  }

  @Test
  public void whenAllTypesAnnotatedWith$thenDummyClassIsNotFound() {
    assertTrue(
        Utils.allTypesAnnotatedWith("com.github.dakusui.scriptiveunit", RunWith.class)
            .noneMatch(DummyClassNotAnnotatedWithRunWith.class::equals)
    );
  }

  @Test
  public void whenAllTypesUnderNonExistingPackageAnnotatedWith() {
    assertTrue(
        Utils.allTypesAnnotatedWith("com.github.dakusui.non.existing", RunWith.class)
            .noneMatch(UtilsTest.class::equals)
    );
  }

  @Test
  public void whenTryToFindExistingTestScript$thenFound() {
    assertTrue(Utils.allScriptsUnder("tests").anyMatch((String s) -> s.equals("tests/testbase.json")));
  }

  @Test
  public void whenTryToFindNonExistingTestScript$thenNotFound() {
    assertTrue(Utils.allScriptsUnder("tests").noneMatch((String s) -> s.equals("tests/non-existing.json")));
  }
}
