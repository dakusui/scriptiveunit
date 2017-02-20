package com.github.dakusui.scriptiveunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestOracle;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
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
import static com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type.OrderBy.TEST_CASE;
import static com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type.OrderBy.TEST_ORACLE;
import static com.github.dakusui.scriptiveunit.core.Utils.filterSingleLevelFactorsOut;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public final class GroupedTestItemRunner extends ParentRunner<Action> {
  public enum Type {
    GROUP_BY_TEST_ORACLE {
      @Override
      Iterable<Runner> createRunners(ScriptiveUnit scriptiveUnit, TestSuiteDescriptor testSuiteLoader) {
        return scriptiveUnit.createRunnersGroupingByTestOracle(testSuiteLoader);
      }
    },
    GROUP_BY_TEST_CASE {
      @Override
      Iterable<Runner> createRunners(ScriptiveUnit scriptiveUnit, TestSuiteDescriptor testSuiteDescriptor) {
        return scriptiveUnit.createRunnersGroupingByTestCase(testSuiteDescriptor);
      }
    },
    GROUP_BY_TEST_FIXTURE {
      @Override
      Iterable<Runner> createRunners(ScriptiveUnit scriptiveUnit, TestSuiteDescriptor testSuiteDescriptor) {
        return scriptiveUnit.createRunnersGroupingByTestFixture(testSuiteDescriptor);
      }

      @Override
      OrderBy orderBy() {
        return TEST_CASE;
      }
    },
    GROUP_BY_TEST_FIXTURE_ORDER_BY_TEST_ORACLE {
      @Override
      Iterable<Runner> createRunners(ScriptiveUnit scriptiveUnit, TestSuiteDescriptor testSuiteDescriptor) {
        return scriptiveUnit.createRunnersGroupingByTestFixture(testSuiteDescriptor);
      }

      @Override
      OrderBy orderBy() {
        return TEST_ORACLE;
      }
    },;

    enum OrderBy {
      TEST_CASE {
        Stream<Action> buildSortedActionStreamOrderingBy(List<IndexedTestCase> testCases, AtomicInteger i, TestSuiteDescriptor testSuiteDescriptor) {
          List<? extends TestOracle> testOracles = testSuiteDescriptor.getTestOracles();
          List<Factor> factors = testSuiteDescriptor.getFactorSpaceDescriptor().getFactors();
          String testSuiteDescription = testSuiteDescriptor.getDescription();
          return testCases.stream()
              .flatMap(eachTestCase -> testOracles.stream()
                  .map(eachOracle ->
                      eachOracle.createTestActionSupplier(factors, i.getAndIncrement(), testSuiteDescription, eachTestCase.getTuple()).get()));
        }
      },
      TEST_ORACLE {
        @Override
        Stream<Action> buildSortedActionStreamOrderingBy(List<IndexedTestCase> testCases, AtomicInteger i, TestSuiteDescriptor testSuiteDescriptor) {
          List<? extends TestOracle> testOracles = testSuiteDescriptor.getTestOracles();
          List<Factor> factors = testSuiteDescriptor.getFactorSpaceDescriptor().getFactors();
          String testSuiteDescription = testSuiteDescriptor.getDescription();
          return testOracles.stream()
              .flatMap(eachOracle -> testCases.stream()
                  .map(eachTestCase ->
                      eachOracle.createTestActionSupplier(factors, i.getAndIncrement(), testSuiteDescription, eachTestCase.getTuple()).get()));
        }
      };

      abstract Stream<Action> buildSortedActionStreamOrderingBy(List<IndexedTestCase> testCases, AtomicInteger i, TestSuiteDescriptor testSuiteDescriptor);
    }


    abstract Iterable<Runner> createRunners(ScriptiveUnit scriptiveUnit, TestSuiteDescriptor testSuiteDescriptor);

    OrderBy orderBy() {
      throw new UnsupportedOperationException();
    }
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

  static GroupedTestItemRunner createRunnerForTestOracle(Class<?> testClass, int testOracleId, TestOracle testOracle, TestSuiteDescriptor testSuiteDescriptor) {
    List<Factor> factors = testSuiteDescriptor.getFactorSpaceDescriptor().getFactors();
    String testSuiteDescription = testSuiteDescriptor.getDescription();
    List<IndexedTestCase> testCases = testSuiteDescriptor.getTestCases();
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
                                requireNonNull(createSetUpAction(testSuiteDescriptor, input.getTuple()))
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
      throw ScriptiveUnitException.wrap(initializationError);
    }
  }

  static GroupedTestItemRunner createRunnerForTestCase(Class<?> testClass, IndexedTestCase testCase, TestSuiteDescriptor testSuiteDescriptor) {
    int testCaseId = testCase.getIndex();
    List<Factor> factors = testSuiteDescriptor.getFactorSpaceDescriptor().getFactors();
    String testSuiteDescription = testSuiteDescriptor.getDescription();
    List<? extends TestOracle> testOracles = testSuiteDescriptor.getTestOracles();
    try {
      Tuple testCaseTuple = testCase.getTuple();
      return new GroupedTestItemRunner(testClass,
          concat(
              of(
                  named(
                      format("%03d: Setup test fixture", 0),
                      named(format("fixture: %s", filterSingleLevelFactorsOut(testCase.getTuple(), factors)),
                          requireNonNull(createSetUpAction(testSuiteDescriptor, testCaseTuple)))
                  )),
              testOracles.stream()
                  .map((TestOracle input) -> input.createTestActionSupplier(factors, testOracles.indexOf(input) + 1, testSuiteDescription, testCaseTuple).get()))
              .collect(toList()),
          testCaseId);
    } catch (InitializationError initializationError) {
      throw ScriptiveUnitException.wrap(initializationError);
    }
  }

  static GroupedTestItemRunner createRunnerForTestFixture(Class<?> testClass, int fixtureId, Tuple fixture, List<IndexedTestCase> testCasesFilteredByFixture, TestSuiteDescriptor testSuiteDescriptor) {
    try {
      AtomicInteger i = new AtomicInteger(0);
      return new GroupedTestItemRunner(
          testClass,
          concat(
              of(
                  named(
                      format("%03d: Setup test fixture", i.getAndIncrement()),
                      named(format("fixture: %s", fixture),
                          requireNonNull(createSetUpAction(testSuiteDescriptor, fixture)))
                  )),
              testSuiteDescriptor.getRunnerType().orderBy().buildSortedActionStreamOrderingBy(testCasesFilteredByFixture, i, testSuiteDescriptor)
          ).collect(toList()),
          fixtureId);
    } catch (InitializationError initializationError) {
      throw ScriptiveUnitException.wrap(initializationError);
    }
  }

  private static Action createSetUpAction(TestSuiteDescriptor testSuiteDescriptor, Tuple input) {
    return testSuiteDescriptor.getSetUpActionFactory().apply(Stage.Type.SETUP.create(testSuiteDescriptor, input));
  }
}
