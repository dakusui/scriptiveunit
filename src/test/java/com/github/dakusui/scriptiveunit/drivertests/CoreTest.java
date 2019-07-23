package com.github.dakusui.scriptiveunit.drivertests;

import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.scriptiveunit.libs.Core;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.asObject;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.scriptiveunit.testutils.UtUtils.createFrameworkStage;
import static com.github.dakusui.scriptiveunit.testutils.UtUtils.createOracleStage;

public class CoreTest extends TestBase {
  private final Core lib = new Core();

  @Test
  public void givenOracleStage$whenException$thenExceptionReturned() {
    final Stage stage = createOracleStage();
    Object value = lib.exception().apply(stage);
    System.out.println(value);
    assertThat(
        value,
        asObject().isInstanceOf(Exception.class).$());
  }

  @Test(expected = IllegalStateException.class)
  public void givenFrameworkStage$whenException$thenExceptionThrown() {
    final Stage stage = createFrameworkStage();
    try {
      Object value = lib.exception().apply(stage);
      System.out.println(value);
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }
  }

  @Test
  public void givenOracleStage$whenTestCase$thenTestCaseReturned() {
    final Stage stage = createOracleStage();
    Object value = lib.testCase().apply(stage);
    System.out.println(value);
    assertThat(
        value,
        asObject().isInstanceOf(TestCase.class).$());
  }

  @Test(expected = IllegalStateException.class)
  public void givenFrameworkStage$whenTestCase$thenExceptionThrown() {
    final Stage stage = createFrameworkStage();
    try {
      Object value = lib.testCase().apply(stage);
      System.out.println(value);
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }
  }

  @Test
  public void givenOracleStage$whenTestOracle$thenTestOracleReturned() {
    final Stage stage = createOracleStage();
    Object value = lib.testOracle().apply(stage);
    System.out.println(value);
    assertThat(
        value,
        asObject().isInstanceOf(TestOracle.class).$());
  }

  @Test(expected = IllegalStateException.class)
  public void givenFrameworkStage$whenTestOracle$thenExceptionThrown() {
    final Stage stage = createFrameworkStage();
    try {
      Object value = lib.testOracle().apply(stage);
      System.out.println(value);
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }
  }

}
