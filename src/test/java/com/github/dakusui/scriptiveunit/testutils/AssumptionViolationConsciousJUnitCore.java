package com.github.dakusui.scriptiveunit.testutils;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import static java.util.Objects.requireNonNull;

/**
 * Conventional JUnitCore does not count tests failed with {@link org.junit.AssumptionViolatedException} ignored.
 * This behavior makes tests for a test runner hard since there will be no way to make sure a test case threw
 * an AssumptionViolatedException appropriately.
 *
 * This class provides a way to count such tests that threw the exception by accessing the value by {@code getIgnoreCount}
 * method.
 */
public class AssumptionViolationConsciousJUnitCore extends JUnitCore {

  private RunNotifier notifier = new RunNotifier();

  AssumptionViolationConsciousJUnitCore() {
  }

  @Override
  public Result run(org.junit.runner.Runner runner) {
    Result result = new CustomResult();
    RunListener listener = result.createListener();
    notifier().addFirstListener(listener);
    try {
      notifier().fireTestRunStarted(runner.getDescription());
      runner.run(notifier());
      notifier().fireTestRunFinished(result);
    } finally {
      removeListener(listener);
    }
    return result;
  }

  /**
   * Add a listener to be notified as the tests run.
   *
   * @param listener the listener to add
   * @see RunListener
   */
  @Override
  public void addListener(RunListener listener) {
    notifier.addListener(listener);
  }

  /**
   * Remove a listener.
   *
   * @param listener the listener to remove
   */
  @Override
  public void removeListener(RunListener listener) {
    notifier.removeListener(listener);
  }

  private RunNotifier notifier() {
    return notifier;
  }

  static class CustomResult extends Result {
    public RunListener createListener() {
      RunListener target = super.createListener();
      return new RunListener() {
        ThreadLocal<Description> description = new ThreadLocal<>();

        @Override
        public void testRunStarted(Description description) throws Exception {
          this.description.set(description);
          target.testRunStarted(description);
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
          target.testRunFinished(result);
        }

        @Override
        public void testFinished(Description description) throws Exception {
          target.testFinished(description);
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
          target.testFailure(failure);
        }

        @Override
        public void testIgnored(Description description) throws Exception {
          target.testIgnored(description);
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
          try {
            target.testAssumptionFailure(failure);
            this.testIgnored(requireNonNull(this.description.get()));
          } catch (Exception e) {
            throw ScriptiveUnitException.wrapIfNecessary(e);
          }
        }
      };
    }
  }
}
