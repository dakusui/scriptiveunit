package com.github.dakusui.scriptiveunit.unittests.issues;

import com.github.dakusui.scriptiveunit.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.examples.Qapi;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.faultsource.printable.Predicates.isEmpty;
import static org.junit.runner.JUnitCore.runClasses;

public class Issue28Test extends TestBase {
  @RunWith(ScriptiveSuiteSet.class)
  @ScriptiveSuiteSet.SuiteScripts(
      driverClass = Qapi.class,
      includes = { ".*issue-28.json" }
  )
  public static class Issue28 {
  }

  @RunWith(ScriptiveSuiteSet.class)
  @ScriptiveSuiteSet.SuiteScripts(
      driverClass = Qapi.class,
      includes = { ".*issue-28a.json" }
  )
  public static class Issue28a {
  }

  @RunWith(ScriptiveSuiteSet.class)
  @ScriptiveSuiteSet.SuiteScripts(
      driverClass = Qapi.class,
      includes = { ".*issue-28b-fails.json" }
  )
  public static class Issue28bFails {
  }

  @RunWith(ScriptiveSuiteSet.class)
  @ScriptiveSuiteSet.SuiteScripts(
      driverClass = Qapi.class,
      includes = { ".*issue-28b-passes.json" }
  )
  public static class Issue28bPasses {
  }

  @RunWith(ScriptiveSuiteSet.class)
  @ScriptiveSuiteSet.SuiteScripts(
      driverClass = Qapi.class,
      includes = { ".*issue-28-regression.json" }
  )
  public static class Issue28Regression {
  }

  @RunWith(ScriptiveSuiteSet.class)
  @ScriptiveSuiteSet.SuiteScripts(
      driverClass = Qapi.class,
      includes = { ".*print_twice.json" }
  )
  public static class Issue28RegressionPrintTwice {
  }

  @RunWith(ScriptiveSuiteSet.class)
  @ScriptiveSuiteSet.SuiteScripts(
      driverClass = Qapi.class,
      includes = { ".*issue-28-regression-nested-call.json" }
  )
  public static class Issue28RegressionPrintTwiceNested {
  }

  @Test
  public void testIssue28RegressionPrintTwiceNested() {
    Result result = runClasses(Issue28RegressionPrintTwiceNested.class);
    for (int i = 0; i < result.getFailures().size(); i++) {
      printFailure(result.getFailures().get(i));
    }
    assertThat(
        result,
        asListOf(Failure.class, Result::getFailures)
            .check(isEmpty())
            .$()
    );
  }

  @Test
  public void testIssue28RegressionPrintTwice() {
    Result result = runClasses(Issue28RegressionPrintTwice.class);
    for (int i = 0; i < result.getFailures().size(); i++) {
      printFailure(result.getFailures().get(i));
    }
    assertThat(
        result,
        asListOf(Failure.class, Result::getFailures)
            .check(isEmpty())
            .$()
    );
  }

  @Test
  public void testIssue28Regression() {
    Result result = runClasses(Issue28Regression.class);
    for (int i = 0; i < result.getFailures().size(); i++) {
      printFailure(result.getFailures().get(i));
    }
    assertThat(
        result,
        asListOf(Failure.class, Result::getFailures)
            .check(isEmpty())
            .$()
    );
  }

  @Test
  public void testIssue28() {
    Result result = runClasses(Issue28.class);
    for (int i = 0; i < result.getFailures().size(); i++) {
      printFailure(result.getFailures().get(i));
    }
    assertThat(
        result,
        asListOf(Failure.class, Result::getFailures)
            .check(isEmpty())
            .$()
    );
  }

  @Test
  public void testIssue28a() {
    Result result = runClasses(Issue28a.class);
    for (int i = 0; i < result.getFailures().size(); i++) {
      printFailure(result.getFailures().get(i));
    }
    assertThat(
        result,
        asListOf(Failure.class, Result::getFailures)
            .check(isEmpty())
            .$()
    );
  }

  @Test
  public void testIssue28bPasses() {
    Result result = runClasses(Issue28bPasses.class);
    for (int i = 0; i < result.getFailures().size(); i++) {
      printFailure(result.getFailures().get(i));
    }
    assertThat(
        result,
        asListOf(Failure.class, Result::getFailures)
            .check(isEmpty())
            .$()
    );
  }


  @Test
  public void testIssue28bFails() {
    Result result = runClasses(Issue28bFails.class);
    for (int i = 0; i < result.getFailures().size(); i++) {
      printFailure(result.getFailures().get(i));
    }
    assertThat(
        result,
        asListOf(Failure.class, Result::getFailures)
            .check(isEmpty())
            .$()
    );
  }


  private void printFailure(Failure failure) {
    System.err.printf(
        "%s:%s%n%s",
        failure.getTestHeader(),
        failure.getDescription(),
        failure.getMessage()
    );
    failure.getException().printStackTrace();
  }
}
