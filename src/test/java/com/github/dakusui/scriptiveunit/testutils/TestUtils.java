package com.github.dakusui.scriptiveunit.testutils;

import com.github.dakusui.scriptiveunit.core.Config;

public enum TestUtils {
  ;

  public static boolean isRunUnderSurefire() {
    return System.getProperty("surefire.real.class.path") != null;
  }

  public static void configureScriptNameSystemProperty(String scriptName, Class driverClass) {
    String scriptSystemPropertyKey = new Config.Builder(driverClass, System.getProperties()).build().getScriptResourceNameKey();
    System.setProperty(scriptSystemPropertyKey, scriptName);
  }
}
