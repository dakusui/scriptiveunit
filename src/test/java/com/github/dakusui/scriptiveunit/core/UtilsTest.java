package com.github.dakusui.scriptiveunit.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

public class UtilsTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
  @Test
  public void main() {
    LOGGER.debug("Hello");
    //      System.out.println(new Reflections("tests", new ResourcesScanner()).getResources(Pattern.compile(".json")));
    System.out.println(Utils.allScriptsUnder("tests").collect(toList()));
    System.out.println(Utils.allTypesAnnotatedWith("com", RunWith.class));
  }
}
