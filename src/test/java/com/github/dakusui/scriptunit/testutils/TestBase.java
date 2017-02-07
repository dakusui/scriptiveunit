package com.github.dakusui.scriptunit.testutils;

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
  public void suppressStdOutErr() {
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

  @Before
  public void keepSystemProperties() {
    this.systemProperties = (Properties) System.getProperties().clone();
  }

  @After
  public void restoreSystemProperties() {
    System.setProperties(this.systemProperties);
  }

  @After
  public void restoreStdOutErr() {
    System.setOut(stdout);
    System.setOut(stderr);
  }
}
