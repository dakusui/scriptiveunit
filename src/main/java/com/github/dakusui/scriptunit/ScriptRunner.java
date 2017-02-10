package com.github.dakusui.scriptunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.scriptunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptunit.loaders.TestSuiteLoader;
import com.github.dakusui.scriptunit.model.Func;
import com.github.dakusui.scriptunit.model.Stage;
import com.github.dakusui.scriptunit.model.TestOracle;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public abstract class ScriptRunner extends ParentRunner<Action> {
  public enum Type {
    GROUP_BY_TEST_ORACLE {
      @Override
      Iterable<Runner> createRunners(ScriptUnit scriptUnit, TestSuiteLoader testSuiteLoader) {
        return scriptUnit.createRunnersGroupingByTestOracle(testSuiteLoader);
      }
    },
    GROUP_BY_TEST_CASE {
      @Override
      Iterable<Runner> createRunners(ScriptUnit scriptUnit, TestSuiteLoader testSuiteLoader) {
        return scriptUnit.createRunnersGroupingByTestCase(testSuiteLoader);
      }
    },
    GROUP_BY_TEST_FIXTURE {
      @Override
      Iterable<Runner> createRunners(ScriptUnit scriptUnit, TestSuiteLoader testSuiteLoader) {
        return scriptUnit.createRunnersGroupingByTestFixture(testSuiteLoader);
      }
    },;

    abstract Iterable<Runner> createRunners(ScriptUnit scriptUnit, TestSuiteLoader testSuiteLoader);
  }

  private final List<Action> actions;
  private final int          groupId;


  /**
   * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
   */
  ScriptRunner(Class<?> testClass, List<Action> actions, int groupId) throws InitializationError {
    super(testClass);
    this.actions = requireNonNull(actions);
    this.groupId = groupId;
  }

  @Override
  protected void runChild(Action child, RunNotifier notifier) {
    Description description = describeChild(child);
    if (isIgnored(child)) {
      notifier.fireTestIgnored(description);
    } else {
      runLeaf(actionBlock(child), description, notifier);
    }
  }

  @Override
  protected Description describeChild(Action action) {
    return Description.createTestDescription(
        getTestClass().getJavaClass(),
        testName(action)
    );
  }

  @Override
  protected Annotation[] getRunnerAnnotations() {
    return new Annotation[0];
  }

  @Override
  protected List<Action> getChildren() {
    return this.actions;
  }

  @Override
  protected String getName() {
    return format("[%d]", this.groupId);
  }

  private Statement actionBlock(Action action) {
    return actionInvoker(action);
  }

  private String testName(Action action) {
    return format("%s[%d]", action.toString(), this.groupId);
  }

  private Statement actionInvoker(Action action) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        ActionRunner.WithResult runner = new ActionRunner.WithResult();
        try {
          action.accept(runner);
        } finally {
          action.accept(runner.createPrinter());
        }
      }
    };
  }

  static class GroupingByTestOracle extends ScriptRunner {
    GroupingByTestOracle(Class<?> testClass, String testSuiteDescription, int testOracleId, TestOracle testOracle, Func<Stage, Action> setUpFactory, List<IndexedTestCase> testCases) throws InitializationError {
      super(
          testClass,
          testCases.stream()
              .map(input -> testOracle.createTestAction(testSuiteDescription, testOracleId, input.getIndex(), input, setUpFactory))
              .collect(toList()),
          testOracleId
      );
    }
  }

  static class GroupingByTestCase extends ScriptRunner {
    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     *
     * @param testClass A test class.
     */
    GroupingByTestCase(Class<?> testClass, String testSuiteDescription, int testCaseId, TestCase testCase, Func<Stage, Action> setUpFactory, List<TestOracle> testOracles)
        throws InitializationError {
      super(
          testClass,
          testOracles.stream()
              .map((TestOracle input) -> input.createTestAction(testSuiteDescription, testCaseId, testOracles.indexOf(input), testCase, null))
              .collect(toList()),
          testCaseId
      );
    }
  }

  static class GroupingByFixture extends ScriptRunner {

    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     *
     * @param testClass A test class.
     */
    GroupingByFixture(Class<?> testClass, List<Action> actions, int groupId) throws InitializationError {
      super(testClass, actions, groupId);
    }
  }
}
