package com.github.dakusui.scriptiveunit.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class UtilsTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

  @Test
  public void whenAllTypesAnnotatedWith$thenThisClassIsFound() {
    LOGGER.debug("Hello");
    assertTrue(
        Utils.allTypesAnnotatedWith("com.rakuten.gsp", RunWith.class)
            .anyMatch(UtilsTest.class::equals)
    );
  }

  @Test
  public void whenAllTypesAnnotatedWith$thenDummyClassIsNotFound() {
    LOGGER.debug("Hello");
    assertTrue(
        !Utils.allTypesAnnotatedWith("com.rakuten.gsp", RunWith.class)
            .anyMatch(DummyClassNotAnnotatedWithRunWith.class::equals)
    );
  }

  @Test
  public void findTestScripts() {
    assertTrue(Utils.allScriptsUnder("tests").anyMatch((String s) -> s.equals("tests/testbase.json")));
  }
}
