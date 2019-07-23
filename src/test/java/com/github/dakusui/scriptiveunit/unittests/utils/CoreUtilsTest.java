package com.github.dakusui.scriptiveunit.unittests.utils;

import com.github.dakusui.scriptiveunit.testutils.TestBase;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static com.github.dakusui.scriptiveunit.utils.CoreUtils.singletonCollector;

public class CoreUtilsTest extends TestBase {

  @Test(expected = IllegalStateException.class)
  public void givenMultiElements$testSingletonCollector() {
    try {
      System.out.println(Stream.of("hello", "world").collect(singletonCollector(NoSuchElementException::new, IllegalStateException::new)));
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }
  }

  @Test(expected = IllegalStateException.class)
  public void givenMultiElements$testSingletonCollector_() {
    try {
      System.out.println(Stream.of("hello", "world", "!").filter(each -> !each.equals("hello")).collect(singletonCollector(NoSuchElementException::new, IllegalStateException::new)));
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }
  }

  @Test(expected = NoSuchElementException.class)
  public void givenNoElement$testSingletonCollector() {
    try {
      System.out.println(Stream.empty().collect(singletonCollector(NoSuchElementException::new, IllegalStateException::new)));
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }
  }

}
