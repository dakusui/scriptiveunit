package com.github.dakusui.scriptiveunit.unittests.issues;

import com.github.dakusui.scriptiveunit.drivers.actions.Basic;
import com.github.dakusui.scriptiveunit.examples.Qapi;
import com.github.dakusui.scriptiveunit.testutils.TestUtils;
import org.junit.Test;
import org.junit.runner.Computer;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.util.LinkedList;
import java.util.List;

public class Issue37Test {
  public abstract static class Base {

    @Test
    public void test() {
      List<String> out = new LinkedList<>();
      Basic.setOut(out::add);
      TestUtils.configureScriptNameSystemProperty(getScriptName(), Qapi.class);
      runTests();
      out.forEach(System.out::println);
    }

    void runTests() {
      Runner runner = Request.classes(new Computer(), Qapi.class).getRunner();
      runner.run(new RunNotifier());
      System.err.println(runner.getDescription());
    }

    public abstract String getScriptName();
  }

  /**
   * <pre>
   * fireTestStarted :000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :000: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * </pre>
   */
  public static class GroupByTestOracle extends Base {
    @Override
    public String getScriptName() {
      return "tests/issues/issue-37-groupByTestOracle.json";
    }
  }

  /**
   * <pre>
   * fireTestStarted :000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :000: Oracle A[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle A[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * </pre>
   */
  public static class GroupByTestCase extends Base {
    @Override
    public String getScriptName() {
      return "tests/issues/issue-37-groupByTestCase.json";
    }
  }

  /**
   * <pre>
   * fireTestStarted :000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :002: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:002: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :003: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:003: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * </pre>
   */
  public static class GroupByTestFixture extends Base {
    @Override
    public String getScriptName() {
      return "tests/issues/issue-37-groupByTestFixture.json";
    }
  }

  /**
   * <pre>
   * fireTestStarted :000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :002: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:002: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :003: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:003: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * </pre>
   */
  public static class GroupByTestFixtureOreringByTestOracle extends Base {
    @Override
    public String getScriptName() {
      return "tests/issues/issue-37-groupByTestFixtureOrderingByTestOracle.json";
    }
  }
}
