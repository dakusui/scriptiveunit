package com.github.dakusui.scriptiveunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestItem;
import com.github.dakusui.scriptiveunit.model.TestOracle;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.Actions.named;
import static com.github.dakusui.actionunit.Actions.nop;
import static com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type.OrderBy.TEST_CASE;
import static com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type.OrderBy.TEST_ORACLE;
import static com.github.dakusui.scriptiveunit.core.Utils.*;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.SETUP;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.TEARDOWN;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class GroupedTestItemRunner extends ParentRunner<Action> {
  private static Iterable<Runner> createRunnersGroupingByTestOracle(final Session session) {
    TestSuiteDescriptor testSuiteDescriptor = session.getDescriptor();
    return testSuiteDescriptor.getTestOracles()
        .stream()
        .map(new Function<TestOracle, Runner>() {
          int id = 0;

          @Override
          public Runner apply(TestOracle input) {
            return createRunnerForTestOracle(
                session.getConfig().getDriverObject().getClass(),
                id++,
                input,
                session);
          }
        })
        .collect(toList());
  }

  private static Iterable<Runner> createRunnersGroupingByTestCase(final Session session) {
    TestSuiteDescriptor testSuiteDescriptor = session.getDescriptor();
    return testSuiteDescriptor.getTestCases().stream().map(
        (Function<IndexedTestCase, Runner>) (IndexedTestCase testCase) -> createRunnerForTestCase(
            session.getConfig().getDriverObject().getClass(),
            testCase,
            session)).collect(toList());
  }

  private static Iterable<Runner> createRunnersGroupingByTestFixture(Session session) {
    TestSuiteDescriptor testSuiteDescriptor = session.getDescriptor();
    List<Factor> factors = testSuiteDescriptor.getFactorSpaceDescriptor().getFactors();
    List<String> singleLevelFactors = factors.stream()
        .filter((Factor each) -> each.levels.size() == 1)
        .map((Factor each) -> each.name)
        .collect(toList());
    List<String> involved = figureOutInvolvedParameters(testSuiteDescriptor, singleLevelFactors);
    List<IndexedTestCase> testCases = testSuiteDescriptor.getTestCases().stream()
        .sorted(byParameters(involved))
        .collect(toList());
    List<Tuple> fixtures = buildFixtures(involved, testCases);
    return fixtures.stream().map(
        (Function<Tuple, Runner>) fixture -> createRunnerForTestFixture(
            session.getConfig().getDriverObject().getClass(),
            fixtures.indexOf(fixture), fixture,
            testCases.stream().filter((IndexedTestCase indexedTestCase) -> project(indexedTestCase.getTuple(), involved).equals(fixture)).collect(toList()),
            session)
    ).collect(toList());
  }

  private static List<String> figureOutInvolvedParameters(TestSuiteDescriptor testSuiteDescriptor, List<String> singleLevelFactors) {
    return Stream.concat(testSuiteDescriptor.getInvolvedParameterNamesInSetUpAction().stream(), singleLevelFactors.stream()).distinct().collect(toList());
  }

  private static LinkedList<Tuple> buildFixtures(List<String> involved, List<IndexedTestCase> testCases) {
    return new LinkedList<>(
        testCases.stream()
            .map((IndexedTestCase input) -> project(input.getTuple(), involved))
            .map((Map<String, Object> input) -> new Tuple.Builder().putAll(input).build())
            .collect(toSet()));
  }

  private static <K, V> Map<K, V> project(Map<K, V> in, List<K> keys) {
    Map<K, V> ret = new HashMap<>();
    keys.forEach(each -> {
      if (in.containsKey(each))
        ret.put(each, in.get(each));
    });
    return ret;
  }

  private static Comparator<? super TestCase> byParameters(List<String> parameters) {
    return (Comparator<TestCase>) (o1, o2) -> {
      for (String each : parameters) {
        int ret = Objects.toString(o1.getTuple().get(each)).compareTo(Objects.toString(o2.getTuple().get(each)));
        if (ret != 0)
          return ret;
      }
      return 0;
    };
  }

  public enum Type {
    GROUP_BY_TEST_ORACLE {
      @Override
      Iterable<Runner> createRunners(Session session) {
        return createRunnersGroupingByTestOracle(session);
      }
    },
    GROUP_BY_TEST_CASE {
      @Override
      Iterable<Runner> createRunners(Session session) {
        return createRunnersGroupingByTestCase(session);
      }
    },
    GROUP_BY_TEST_FIXTURE {
      @Override
      Iterable<Runner> createRunners(Session session) {
        return createRunnersGroupingByTestFixture(session);
      }

      @Override
      OrderBy orderBy() {
        return TEST_CASE;
      }
    },
    GROUP_BY_TEST_FIXTURE_ORDER_BY_TEST_ORACLE {
      @Override
      Iterable<Runner> createRunners(Session session) {
        return createRunnersGroupingByTestFixture(session);
      }

      @Override
      OrderBy orderBy() {
        return TEST_ORACLE;
      }
    },;

    enum OrderBy {
      TEST_CASE {
        Stream<Action> buildSortedActionStreamOrderingBy(Session session, List<IndexedTestCase> testCases, AtomicInteger i) {
          TestSuiteDescriptor testSuiteDescriptor = session.getDescriptor();
          List<? extends TestOracle> testOracles = testSuiteDescriptor.getTestOracles();
          return testCases.stream()
              .flatMap(eachTestCase -> testOracles.stream()
                  .map(eachOracle ->
                      eachOracle
                          .createTestActionFactory(
                              TestItem.create(eachTestCase.getIndex(), eachOracle.getIndex(), i.getAndIncrement()),
                              eachTestCase.getTuple()
                          ).apply(session)
                  ));
        }
      },
      TEST_ORACLE {
        @Override
        Stream<Action> buildSortedActionStreamOrderingBy(Session session, List<IndexedTestCase> testCases, AtomicInteger i) {
          TestSuiteDescriptor testSuiteDescriptor = session.getDescriptor();
          List<? extends TestOracle> testOracles = testSuiteDescriptor.getTestOracles();
          return testOracles.stream()
              .flatMap(eachOracle -> testCases.stream()
                  .map(eachTestCase ->
                      eachOracle.createTestActionFactory(
                          TestItem.create(eachTestCase.getIndex(), eachOracle.getIndex(), i.getAndIncrement()),
                          eachTestCase.getTuple()
                      ).apply(session)));
        }
      };

      abstract Stream<Action> buildSortedActionStreamOrderingBy(Session session, List<IndexedTestCase> testCases, AtomicInteger i);
    }


    abstract Iterable<Runner> createRunners(Session session);

    OrderBy orderBy() {
      throw new UnsupportedOperationException();
    }
  }

  private final int          groupId;
  private final Action       beforeAction;
  private final List<Action> mainActions;
  private final Action       afterAction;

  /**
   * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
   */
  private GroupedTestItemRunner(Class<?> testClass, int groupId, Action beforeAction, List<Action> mainActions, Action afterAction) throws InitializationError {
    super(testClass);
    this.groupId = groupId;
    this.beforeAction = beforeAction;
    this.mainActions = requireNonNull(mainActions);
    this.afterAction = afterAction;
  }

  @Override
  protected Statement withBeforeClasses(Statement statement) {
    return new RunBefores(statement, Collections.emptyList(), null) {
      @Override
      public void evaluate() throws Throwable {
        super.evaluate();
        Utils.performActionWithLogging(beforeAction);
      }
    };
  }

  @Override
  protected Statement withAfterClasses(Statement statement) {
    return new RunAfters(statement, Collections.emptyList(), null) {
      @Override
      public void evaluate() throws Throwable {
        super.evaluate();
        Utils.performActionWithLogging(afterAction);
      }
    };
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
    return this.mainActions;
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

  private static GroupedTestItemRunner createRunnerForTestOracle(Class<?> testClass, int testOracleId, TestOracle testOracle, Session session) {
    TestSuiteDescriptor testSuiteDescriptor = session.getDescriptor();
    List<Factor> factors = testSuiteDescriptor.getFactorSpaceDescriptor().getFactors();
    String testSuiteDescription = testSuiteDescriptor.getDescription();
    List<IndexedTestCase> testCases = testSuiteDescriptor.getTestCases();
    try {
      return new GroupedTestItemRunner(testClass,
          testOracleId,
          nop(),
          testCases.stream()
              .map(new Function<IndexedTestCase, Action>() {
                int i = 0;

                @Override
                public Action apply(IndexedTestCase input) {
                  try {
                    Tuple prettifiedTestCaseTuple = filterSingleLevelFactorsOut(input.getTuple(), factors);
                    return Actions.sequential(
                        format("%03d: %s", i, template(testOracle.getDescription(), append(input.getTuple(), "@TESTSUITE", testSuiteDescription))),
                        named(
                            format("%03d: Setup test fixture", i),
                            named(format("fixture: %s", prettifiedTestCaseTuple),
                                requireNonNull(createFixtureLevelAction(SETUP, session, input.getTuple()))
                            )
                        ),
                        Actions
                            .attempt(
                                testOracle.createTestActionFactory(
                                    TestItem.create(
                                        input.getIndex(),
                                        testOracleId,
                                        input.getIndex()
                                    ),
                                    input.getTuple()
                                ).apply(session))
                            .ensure(
                                named(
                                    format("%03d: Tear down fixture", i),
                                    named(format("fixture: %s", prettifiedTestCaseTuple),
                                        requireNonNull(createFixtureLevelAction(TEARDOWN, session, input.getTuple()))
                                    )
                                ))
                            .build()
                    );
                  } finally {
                    i++;
                  }
                }
              }).collect(toList()),
          nop()
      );
    } catch (InitializationError initializationError) {
      throw ScriptiveUnitException.wrap(initializationError);
    }
  }

  private static GroupedTestItemRunner createRunnerForTestCase(Class<?> testClass, IndexedTestCase testCase, Session session) {
    TestSuiteDescriptor testSuiteDescriptor = session.getDescriptor();
    int testCaseId = testCase.getIndex();
    List<Factor> factors = testSuiteDescriptor.getFactorSpaceDescriptor().getFactors();
    List<? extends TestOracle> testOracles = testSuiteDescriptor.getTestOracles();
    try {
      AtomicInteger i = new AtomicInteger(0);
      Tuple testCaseTuple = testCase.getTuple();
      Tuple prettifiedTestCaseTuple = filterSingleLevelFactorsOut(testCase.getTuple(), factors);
      return new GroupedTestItemRunner(testClass,
          testCaseId,
          named(
              format("%03d: Setup test fixture", i.getAndIncrement()),
              named(format("fixture: %s", prettifiedTestCaseTuple),
                  requireNonNull(createFixtureLevelAction(SETUP, session, testCaseTuple)))),
          testOracles.stream()
              .map((TestOracle input) -> input.createTestActionFactory(
                  TestItem.create(
                      testCaseId,
                      input.getIndex(),
                      input.getIndex()
                  ),
                  testCaseTuple
              ).apply(session))
              .collect(toList()),
          named(
              format("%03d: Tear down fixture", testOracles.size()),
              named(format("fixture: %s", prettifiedTestCaseTuple),
                  requireNonNull(createFixtureLevelAction(TEARDOWN, session, testCaseTuple))
              )
          )
      );
    } catch (InitializationError initializationError) {
      throw ScriptiveUnitException.wrap(initializationError);
    }
  }

  private static GroupedTestItemRunner createRunnerForTestFixture(Class<?> testClass, int fixtureId, Tuple fixture, List<IndexedTestCase> testCasesFilteredByFixture, Session session) {
    TestSuiteDescriptor testSuiteDescriptor = session.getDescriptor();
    try {
      AtomicInteger i = new AtomicInteger(0);
      return new GroupedTestItemRunner(
          testClass,
          fixtureId,
          named(
              format("%03d: Setup test fixture", i.getAndIncrement()),
              named(format("fixture: %s", fixture),
                  requireNonNull(createFixtureLevelAction(SETUP, session, fixture)))),
          testSuiteDescriptor.getRunnerType()
              .orderBy()
              .buildSortedActionStreamOrderingBy(session, testCasesFilteredByFixture, i)
              .collect(toList()),
          named(
              format("%03d: Tear down fixture", testCasesFilteredByFixture.size() + 1),
              named(format("fixture: %s", fixture),
                  requireNonNull(createFixtureLevelAction(TEARDOWN, session, fixture))
              )
          )
      );
    } catch (InitializationError initializationError) {
      throw ScriptiveUnitException.wrap(initializationError);
    }
  }

  private static Action createFixtureLevelAction(Stage.Type stageType, Session session, Tuple input) {
    return stageType.getFixtureLevelActionFactory(session).apply(Stage.Factory.createFixtureLevelStage(stageType, session, input));
  }
}
