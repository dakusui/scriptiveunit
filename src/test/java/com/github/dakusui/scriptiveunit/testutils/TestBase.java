package com.github.dakusui.scriptiveunit.testutils;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class TestBase {
  private Properties systemProperties;

  private PrintStream stdout = System.out;
  private PrintStream stderr = System.err;

  @Before
  public void before() {
    this.suppressStdOutErr();
    this.keepSystemProperties();
  }

  @After
  public void after() {
    this.restoreStdOutErr();
    this.restoreSystemProperties();
  }

  private void suppressStdOutErr() {
    if (TestUtils.isRunUnderSurefire()) {
      System.setOut(new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
      }));
      System.setErr(new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
      }));
    }
  }

  private void keepSystemProperties() {
    this.systemProperties = (Properties) System.getProperties().clone();
  }

  private void restoreSystemProperties() {
    System.setProperties(this.systemProperties);
  }

  private void restoreStdOutErr() {
    System.setOut(stdout);
    System.setOut(stderr);
  }
}
