package com.github.dakusui.scriptiveunit.unittests.issues;

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

public class Issue37Test {
  public abstract static class Base {

    @Test
    public void test() {
      TestUtils.configureScriptNameSystemProperty(getScriptName(), Qapi.class);
      runTests();
    }

    void runTests() {
      RunNotifier runNotifier = new RunNotifier() {
        public void fireTestStarted(final Description description) {
          super.fireTestStarted(description);
          System.err.println("fireTestStarted :" + description);
        }

        public void fireTestFinished(final Description description) {
          super.fireTestFinished(description);
          System.err.println("fireTestFinished:" + description);
        }
      };
      runNotifier.addListener(new RunListener() {
        @Override
        public void testRunStarted(Description description) {
          //          System.err.println("  testRunStarted:" + description.getDisplayName());
        }

        public void testRunFinished(Result result) {
          //          System.err.println("  testRunFinished:" + result);
        }

        /**
         * Called when an atomic test is about to be started.
         *
         * @param description the description of the test that is about to be run
         * (generally a class and method name)
         */
        public void testStarted(Description description) throws Exception {
          //          System.err.println("  testStarted:" + description.getDisplayName());
        }

        /**
         * Called when an atomic test has finished, whether the test succeeds or fails.
         *
         * @param description the description of the test that just ran
         */
        public void testFinished(Description description) throws Exception {
          //          System.err.println("  testFinished:" + description.getDisplayName());
        }
      });
      Runner runner = Request.classes(new Computer(), Qapi.class).getRunner();
      runner.run(runNotifier);
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
