package com.github.dakusui.scriptiveunit.testutils;

import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.junit.runner.Result;

import java.io.OutputStream;
import java.io.PrintStream;

public enum TestUtils {
  ;

  public static boolean isRunUnderSurefire() {
    return System.getProperty("surefire.real.class.path") != null;
  }

  public static void configureScriptNameSystemProperty(String scriptName, Class driverClass) {
    String scriptSystemPropertyKey = new JsonScript.Standard(
        driverClass, System.getProperties())
        .getScriptResourceNameKey()
        .orElseThrow(ScriptiveUnitException::noScriptResourceNameKeyWasGiven);
    System.setProperty(scriptSystemPropertyKey, scriptName);
  }

  public static void suppressStdOutErrIfRunUnderSurefire() {
    if (isRunUnderSurefire()) {
      System.setOut(new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
      }));
      System.setErr(new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
      }));
    }
  }

  public static Result runClasses(Class... classes) {
    return new AssumptionViolationConsciousJUnitCore().run(classes);
  }
}
