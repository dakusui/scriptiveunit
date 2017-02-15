package com.github.dakusui.scriptunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptunit.core.Utils;
import com.github.dakusui.scriptunit.exceptions.ScriptUnitException;
import com.github.dakusui.scriptunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptunit.loaders.TestSuiteLoader;
import com.github.dakusui.scriptunit.model.func.Func;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.Actions.named;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public final class ScriptRunner extends ParentRunner<Action> {
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
  private ScriptRunner(Class<?> testClass, List<Action> actions, int groupId) throws InitializationError {
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
        Utils.performActionWithLogging(action);
      }
    };
  }

  static ScriptRunner createRunnerForTestOracle(Class<?> testClass, String testSuiteDescription, int testOracleId, TestOracle testOracle, Func<Stage, Action> setUpFactory, List<IndexedTestCase> testCases) {
    try {
      return new ScriptRunner(testClass,
          testCases.stream()
              .map(new Function<IndexedTestCase, Action>() {
                int i = 0;

                @Override
                public Action apply(IndexedTestCase input) {
                  try {
                    return Actions.sequential(
                        String.format("%03d: %s: %s", i, testOracle.getDescription(), input.getTuple()),
                        named(
                            format("%03d: Setup test fixture %s", i, input.getTuple()),
                            requireNonNull(createSetUpAction(input.getTuple(), setUpFactory))),
                        testOracle.createTestAction(input.getIndex(), testSuiteDescription, input.getTuple()));
                  } finally {
                    i++;
                  }
                }
              }).collect(toList()),
          testOracleId
      );
    } catch (InitializationError initializationError) {
      throw ScriptUnitException.wrap(initializationError);
    }
  }

  static ScriptRunner createRunnerForTestCase(Class<?> testClass, String testSuiteDescription, int testCaseId, IndexedTestCase testCase, Func<Stage, Action> setUpFactory, List<TestOracle> testOracles) {
    try {
      Tuple testCaseTuple = testCase.getTuple();
      return new ScriptRunner(testClass,
          concat(
              of(
                  named(
                      format("%03d: Setup test fixture %s", 0, testCase.getTuple()),
                      requireNonNull(createSetUpAction(testCaseTuple, setUpFactory))
                  )),
              testOracles.stream()
                  .map((TestOracle input) -> input.createTestAction(testOracles.indexOf(input) + 1, testSuiteDescription, testCaseTuple)))
              .collect(toList()),
          testCaseId);
    } catch (InitializationError initializationError) {
      throw ScriptUnitException.wrap(initializationError);
    }
  }

  static ScriptRunner createRunnerForTestFixture(Class<?> testClass, String testSuiteDescription, int testFixtureId, Tuple fixture, Func<Stage, Action> setUpFactory, List<IndexedTestCase> testCases, List<TestOracle> testOracles) {
    try {
      AtomicInteger i = new AtomicInteger(0);
      return new ScriptRunner(testClass,
          concat(
              of(
                  named(
                      format("%03d: Setup test fixture %s", i.getAndIncrement(), fixture),
                      requireNonNull(createSetUpAction(fixture, setUpFactory))
                  )),
              buildSortedActionStreamOrderingByTestCaseAndThenTestOracle(testSuiteDescription, testCases, testOracles, i)
          ).collect(toList()),
          testFixtureId);
    } catch (InitializationError initializationError) {
      throw ScriptUnitException.wrap(initializationError);
    }
  }

  private static Stream<Action> buildSortedActionStreamOrderingByTestCaseAndThenTestOracle(String testSuiteDescription, List<IndexedTestCase> testCases, List<TestOracle> testOracles, AtomicInteger i) {
    return testCases.stream()
        .flatMap(eachTestCase -> testOracles.stream()
            .map(eachOracle ->
                eachOracle.createTestAction(i.getAndIncrement(), testSuiteDescription, eachTestCase.getTuple())));
  }

  private static Stream<Action> buildSortedActionStreamOrderingByTestOracleAndThenTestCase(String testSuiteDescription, List<IndexedTestCase> testCases, List<TestOracle> testOracles, AtomicInteger i) {
    return testOracles.stream()
        .flatMap(eachOracle -> testCases.stream()
            .map(eachTestCase ->
                eachOracle.createTestAction(i.getAndIncrement(), testSuiteDescription, eachTestCase.getTuple())));
  }

  private static Action createSetUpAction(Tuple input, Func<Stage, Action> setUpFactory) {
    return setUpFactory.apply(Stage.Type.SETUP.create(input));
  }
}
