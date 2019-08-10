package com.github.dakusui.scriptiveunit.testutils;

import org.junit.After;
import org.junit.Before;

import java.io.PrintStream;
import java.util.Properties;

public class TestBase {
  private Properties systemProperties;

  private PrintStream stdout = System.out;
  private PrintStream stderr = System.err;

  @Before
  public void before() {
    TestUtils.suppressStdOutErrIfRunUnderSurefire();
    this.keepSystemProperties();
  }

  @After
  public void after() {
    this.restoreStdOutErr();
    this.restoreSystemProperties();
  }

  private void keepSystemProperties() {
    this.systemProperties = (Properties) System.getProperties().clone();
  }

  private void restoreSystemProperties() {
    System.setProperties(this.systemProperties);
  }

  private void restoreStdOutErr() {
    System.setOut(stdout);
    System.setErr(stderr);
  }
}
