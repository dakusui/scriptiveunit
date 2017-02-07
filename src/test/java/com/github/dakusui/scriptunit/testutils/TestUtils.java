package com.github.dakusui.scriptunit.testutils;

public enum TestUtils {
  ;

  public static boolean isRunUnderSurefire() {
    return System.getProperty("surefire.real.class.path") != null;
  }
}
