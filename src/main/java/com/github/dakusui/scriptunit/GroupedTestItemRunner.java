package com.github.dakusui.scriptunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptunit.core.Utils;
import com.github.dakusui.scriptunit.exceptions.ScriptUnitException;
import com.github.dakusui.scriptunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptunit.loaders.TestSuiteLoader;
import com.github.dakusui.scriptunit.model.Stage;
import com.github.dakusui.scriptunit.model.TestOracle;
import com.github.dakusui.scriptunit.model.func.Func;
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
import static com.github.dakusui.scriptunit.core.Utils.filterSingleLevelFactorsOut;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public final class GroupedTestItemRunner extends ParentRunner<Action> {
  public enum Type {
    BY_TEST_ORACLE {
      @Override
      Iterable<Runner> createRunners(ScriptiveUnit scriptiveUnit, TestSuiteLoader testSuiteLoader) {
        return scriptiveUnit.createRunnersGroupingByTestOracle(testSuiteLoader);
      }
    },
    BY_TEST_CASE {
      @Override
      Iterable<Runner> createRunners(ScriptiveUnit scriptiveUnit, TestSuiteLoader testSuiteLoader) {
        return scriptiveUnit.createRunnersGroupingByTestCase(testSuiteLoader);
      }
    },
    BY_TEST_FIXTURE {
      @Override
      Iterable<Runner> createRunners(ScriptiveUnit scriptiveUnit, TestSuiteLoader testSuiteLoader) {
        return scriptiveUnit.createRunnersGroupingByTestFixture(testSuiteLoader);
      }
    },;

    abstract Iterable<Runner> createRunners(ScriptiveUnit scriptiveUnit, TestSuiteLoader testSuiteLoader);
  }

  private final List<Action> actions;
  private final int          groupId;


  /**
   * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
   */
  private GroupedTestItemRunner(Class<?> testClass, List<Action> actions, int groupId) throws InitializationError {
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

  static GroupedTestItemRunner createRunnerForTestOracle(Class<?> testClass, List<Factor> factors, String testSuiteDescription, int testOracleId, TestOracle testOracle, Func<Stage, Action> setUpFactory, List<IndexedTestCase> testCases) {
    try {
      return new GroupedTestItemRunner(testClass,
          testCases.stream()
              .map(new Function<IndexedTestCase, Action>() {
                int i = 0;

                @Override
                public Action apply(IndexedTestCase input) {
                  try {
                    return Actions.sequential(
                        format("%03d: %s", i, testOracle.getDescription()),
                        named(
                            format("%03d: Setup test fixture", i),
                            named(format("fixture: %s", filterSingleLevelFactorsOut(input.getTuple(), factors)),
                                requireNonNull(createSetUpAction(input.getTuple(), setUpFactory))
                            )
                        ),
                        testOracle.createTestActionSupplier(factors, input.getIndex(), testSuiteDescription, input.getTuple()).get());
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

  static GroupedTestItemRunner createRunnerForTestCase(Class<?> testClass, List<Factor> factors, String testSuiteDescription, int testCaseId, IndexedTestCase testCase, Func<Stage, Action> setUpFactory, List<TestOracle> testOracles) {
    try {
      Tuple testCaseTuple = testCase.getTuple();
      return new GroupedTestItemRunner(testClass,
          concat(
              of(
                  named(
                      format("%03d: Setup test fixture", 0),
                      named(format("fixture: %s", filterSingleLevelFactorsOut(testCase.getTuple(), factors)),
                          requireNonNull(createSetUpAction(testCaseTuple, setUpFactory)))
                  )),
              testOracles.stream()
                  .map((TestOracle input) -> input.createTestActionSupplier(factors, testOracles.indexOf(input) + 1, testSuiteDescription, testCaseTuple).get()))
              .collect(toList()),
          testCaseId);
    } catch (InitializationError initializationError) {
      throw ScriptUnitException.wrap(initializationError);
    }
  }

  static GroupedTestItemRunner createRunnerForTestFixture(Class<?> testClass, List<Factor> factors, String testSuiteDescription, int testFixtureId, Tuple fixture, Func<Stage, Action> setUpFactory, List<IndexedTestCase> testCases, List<TestOracle> testOracles) {
    try {
      AtomicInteger i = new AtomicInteger(0);
      return new GroupedTestItemRunner(testClass,
          concat(
              of(
                  named(
                      format("%03d: Setup test fixture", i.getAndIncrement()),
                      named(format("fixture: %s", fixture),
                          requireNonNull(createSetUpAction(fixture, setUpFactory)))
                  )),
              buildSortedActionStreamOrderingByTestCaseAndThenTestOracle(factors, testSuiteDescription, testCases, testOracles, i)
          ).collect(toList()),
          testFixtureId);
    } catch (InitializationError initializationError) {
      throw ScriptUnitException.wrap(initializationError);
    }
  }

  private static Stream<Action> buildSortedActionStreamOrderingByTestCaseAndThenTestOracle(List<Factor> factors, String testSuiteDescription, List<IndexedTestCase> testCases, List<TestOracle> testOracles, AtomicInteger i) {
    return testCases.stream()
        .flatMap(eachTestCase -> testOracles.stream()
            .map(eachOracle ->
                eachOracle.createTestActionSupplier(factors, i.getAndIncrement(), testSuiteDescription, eachTestCase.getTuple()).get()));
  }

  private static Stream<Action> buildSortedActionStreamOrderingByTestOracleAndThenTestCase(List<Factor> factors, String testSuiteDescription, List<IndexedTestCase> testCases, List<TestOracle> testOracles, AtomicInteger i) {
    return testOracles.stream()
        .flatMap(eachOracle -> testCases.stream()
            .map(eachTestCase ->
                eachOracle.createTestActionSupplier(factors, i.getAndIncrement(), testSuiteDescription, eachTestCase.getTuple()).get()));
  }

  private static Action createSetUpAction(Tuple input, Func<Stage, Action> setUpFactory) {
    return setUpFactory.apply(Stage.Type.SETUP.create(input));
  }
}
