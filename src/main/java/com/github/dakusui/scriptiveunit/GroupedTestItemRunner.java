package com.github.dakusui.scriptiveunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.scriptiveunit.action.ActionUtils;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.Session;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.Actions.nop;
import static com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type.OrderBy.TEST_CASE;
import static com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type.OrderBy.TEST_ORACLE;
import static com.github.dakusui.scriptiveunit.action.ActionUtils.createMainActionsForTestCase;
import static com.github.dakusui.scriptiveunit.action.ActionUtils.createMainActionsForTestFixture;
import static com.github.dakusui.scriptiveunit.core.Utils.performActionWithLogging;
import static com.github.dakusui.scriptiveunit.model.func.FuncInvoker.createMemo;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * = Test Case structure in scriptiveunit
 *
 * A fixture is a subtuple of a test case, where attributes involved in a setUp procedure
 * are projected from the test case.
 *
 * For each of running mode, the suite level set up is executed always before all
 * tests.
 * Also the suite level tear down is executed always after all tests.
 *
 * Three running modes:
 *
 * - ByTestOracle
 * - ByTestCase
 * - ByTestFixture
 *
 * == Execution Models of Running Modes
 * In this section, we assume that each test case has two attributes ``F`` and ``T``.
 * ``F`` is used in setUp procedure while ``T`` is not.
 * Also we have three test cases ``TestCase_1``, ``TestCase_2``, and ``TestCase_3``,
 * each of which has following values.
 *
 * |===
 * |           |``F``|``T``
 * |TestCase_1 |x    |100
 * |TestCase_2 |x    |200
 * |TestCase_3 |Y    |300
 * |===
 *
 * === ByTestOracle
 *
 * [ditaa]
 * ----
 * +--------------------------------------------+
 * |Suite                                       |
 * |+------------+ +------------+ +------------+|
 * ||Oracle_a    | |Oracle_b    | |Oracle_c    ||
 * ||+----------+| |+----------+| |+----------+||
 * |||TestCase_1|| ||TestCase_1|| ||TestCase_1|||
 * ||+----------+| |+----------+| |+----------+||
 * ||            | |            | |            ||
 * ||+----------+| |+----------+| |+----------+||
 * |||TestCase_2|| ||TestCase_2|| ||TestCase_2|||
 * ||+----------+| |+----------+| |+----------+||
 * ||            | |            | |            ||
 * ||+----------+| |+----------+| |+----------+||
 * |||TestCase_3|| ||TestCase_3|| ||TestCase_3|||
 * ||+----------+| |+----------+| |+----------+||
 * |+------------+ +------------+ +------------+|
 * +--------------------------------------------+
 * ----
 *
 * === ByTestCase
 * A test runner is created for a ''
 *
 * [ditaa]
 * ----
 * +-----------------------------------------+
 * |Suite                                    |
 * |+-----------+ +-----------+ +-----------+|
 * ||TestCase_1 | |TestCase_2 | |TestCase_3 ||
 * ||+---------+| |+---------+| |+---------+||
 * |||Oracle_a || ||Oracle_a || ||Oracle_a |||
 * ||+---------+| |+---------+| |+---------+||
 * ||           | |           | |           ||
 * ||+---------+| |+---------+| |+---------+||
 * |||Oracle_b || ||Oracle_c || ||Oracle_b |||
 * ||+---------+| |+---------+| |+---------+||
 * ||           | |           | |           ||
 * ||+---------+| |+---------+| |+---------+||
 * |||Oracle_c || ||Oracle_c || ||Oracle_c |||
 * ||+---------+| |+---------+| |+---------+||
 * |+-----------+ +-----------+ +-----------+|
 * +-----------------------------------------+
 * ----
 *
 * === ByTestFixture
 * GroupedTestItemRunner
 *
 * [ditaa]
 * ----
 * +---------------------------------------------+
 * |Suite                                        |
 * |+---------------------------+ +-------------+|
 * ||Fixture_(F:x)              | |Fixture_(F:Y)||
 * ||+-----------+ +-----------+| |+-----------+||
 * |||TestCase_1 | |TestCase_2 || ||TestCase_3 |||
 * |||+---------+| |+---------+|| ||+---------+|||
 * ||||Oracle_a || ||Oracle_a ||| |||Oracle_a ||||
 * |||+---------+| |+---------+|| ||+---------+|||
 * |||           | |           || ||           |||
 * |||+---------+| |+---------+|| ||+---------+|||
 * ||||Oracle_b || ||Oracle_c ||| |||Oracle_b ||||
 * |||+---------+| |+---------+|| ||+---------+|||
 * |||           | |           || ||           |||
 * |||+---------+| |+---------+|| ||+---------+|||
 * ||||Oracle_c || ||Oracle_c ||| |||Oracle_c ||||
 * |||+---------+| |+---------+|| ||+---------+|||
 * ||+-----------+ +-----------+| |+-----------+||
 * |+---------------------------+ +-------------+|
 * +---------------------------------------------+
 * ----
 *
 * === ByTestFixtureOrderedByTestOracle
 *
 * [ditaa]
 * ----
 * +---------------------------------------------------------------------------------------------+
 * |Suite                                                                                        |
 * |+--------------------------------------------+ +--------------------------------------------+|
 * ||Fixture_(F:x)                               | |Fixture_(F:y)                               ||
 * ||+------------+ +------------+ +------------+| |+------------+ +------------+ +------------+||
 * |||Oracle_a    | |Oracle_b    | |Oracle_c    || ||Oracle_a    | |Oracle_b    | |Oracle_c    |||
 * |||+----------+| |+----------+| |+----------+|| ||+----------+| |+----------+| |+----------+|||
 * ||||TestCase_1|| ||TestCase_1|| ||TestCase_1||| |||TestCase_3|| ||TestCase_3|| ||TestCase_3||||
 * |||+----------+| |+----------+| |+----------+|| ||+----------+| |+----------+| |+----------+|||
 * |||            | |            | |            || |+------------+ +------------+ +------------+||
 * |||+----------+| |+----------+| |+----------+|| +--------------------------------------------+|
 * ||||TestCase_2|| ||TestCase_2|| ||TestCase_2|||                                               |
 * |||+----------+| |+----------+| |+----------+||                                               |
 * ||+------------+ +------------+ +------------+|                                               |
 * |+--------------------------------------------+                                               |
 * +---------------------------------------------------------------------------------------------+
 * ----
 *
 * @see GroupedTestItemRunner.Type
 */
public final class GroupedTestItemRunner extends ParentRunner<Action> {
  private static Iterable<Runner> createRunnersGroupingByTestOracle(
      final Session session,
      TestSuiteDescriptor testSuiteDescriptor) {
    AtomicInteger id = new AtomicInteger(0);
    return testSuiteDescriptor.getTestOracles()
        .stream()
        .map((Function<TestOracle, Runner>) input ->
            createRunnerForTestOracle(
                session.getConfig().getDriverObject().getClass(),
                id.getAndIncrement(),
                input,
                session,
                testSuiteDescriptor))
        .collect(toList());
  }

  private static Iterable<Runner> createRunnersGroupingByTestCase(
      final Session session,
      TestSuiteDescriptor testSuiteDescriptor) {
    return testSuiteDescriptor.getTestCases()
        .stream()
        .map(
            (Function<IndexedTestCase, Runner>) (IndexedTestCase testCase) -> createRunnerForTestCase(
                session.getConfig().getDriverObject().getClass(),
                testCase,
                session,
                testSuiteDescriptor)
        ).collect(toList());
  }

  private static Iterable<Runner> createRunnersGroupingByTestFixture(
      Session session,
      TestSuiteDescriptor testSuiteDescriptor) {
    List<Parameter> parameters = testSuiteDescriptor.getFactorSpaceDescriptor().getParameters();
    List<String> singleLevelFactors = parameters.stream()
        .filter((Parameter each) -> each instanceof Parameter.Simple)
        .filter((Parameter each) -> each.getKnownValues().size() == 1)
        .map(Parameter::getName)
        .collect(toList());
    List<String> usedInSetUp = figureOutParametersUsedInSetUp(
        testSuiteDescriptor,
        singleLevelFactors
    );
    List<IndexedTestCase> testCases = testSuiteDescriptor.getTestCases().stream()
        .sorted(byParameters(usedInSetUp))
        .collect(toList());
    List<Tuple> fixtures = buildFixtures(usedInSetUp, testCases);
    return fixtures.stream().map(
        (Function<Tuple, Runner>) fixture -> createRunnerForTestFixture(
            session.getConfig().getDriverObject().getClass(),
            fixtures.indexOf(fixture),
            fixture,
            testCases.stream().filter((IndexedTestCase indexedTestCase) ->
                project(indexedTestCase.get(), usedInSetUp).equals(fixture)).collect(toList()),
            session,
            testSuiteDescriptor)
    ).collect(toList());
  }

  private static List<String> figureOutParametersUsedInSetUp(
      TestSuiteDescriptor testSuiteDescriptor,
      List<String> singleLevelFactors) {
    return Stream.concat(
        testSuiteDescriptor.getInvolvedParameterNamesInSetUpAction().stream(),
        singleLevelFactors.stream()
    ).distinct(
    ).collect(toList());
  }

  private static List<Tuple> buildFixtures(
      List<String> involved,
      List<IndexedTestCase> testCases) {
    return testCases.stream()
        .map((IndexedTestCase input) -> project(input.get(), involved))
        .map((Map<String, Object> input) -> new Tuple.Builder().putAll(input).build())
        .distinct()
        .collect(toCollection(LinkedList::new));
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
        int ret = Objects.toString(o1.get().get(each)).compareTo(Objects.toString(o2.get().get(each)));
        if (ret != 0)
          return ret;
      }
      return 0;
    };
  }

  public enum Type {
    /**
     * byTestOracle::::
     * structure:::
     *
     * [ditaa]
     * ----
     * +-------------------+
     * |ParentRunner       |
     * +---------+---------+
     * |
     * |
     * |                1  +---------------+
     * ^              +--->|withBeforeClass| (nop)
     * |              |    +---------------+
     * |              |                      forEachTestCase; j
     * +------------------+ 1  n  +---------+---------+ 1  | n  +---------------+   setUp[j]
     * |ScriptiveUnit     +------>|GroupItemTestRunner|<>--+--->|MainActions    |   given/when/then[i]
     * |                  | 1     |                   |    |    +---------------+   (withTestCase[j])
     * |                  |<>-+   |groupId            |    |                        tearDown[j]
     * +------------------+   |   +-------------------+    | 1  +---------------+
     * |   forEachTestOracle; i     +--->|withAfterClass | (nop)
     * |                                 +---------------+
     * |
     * |                              1  +---------------+
     * |                           +---->|withBeforeClass| setUpBeforeAll
     * |                           |     +---------------+
     * |                           |
     * +---------------------------+
     * |
     * |  1  +---------------+
     * +---->|withAfterClass | tearDownAfterAll
     * +---------------+
     *
     * ----
     * sequence:::
     *
     * ----
     * setUpBeforeAll (setUpFixture[i] before[i] given[j] when[j] (handleError[j]|then[j]) after[j]) tearDownFixture)+ tearDownBeforeAll
     * ----
     */
    GROUP_BY_TEST_ORACLE {
      @Override
      public Iterable<Runner> createRunners(Session session, TestSuiteDescriptor testSuiteDescriptor) {
        return createRunnersGroupingByTestOracle(session, testSuiteDescriptor);
      }
    },
    /**
     * byTestCase::::
     * structure:::
     * [ditaa]
     * ----
     * +-------------------+
     * |ParentRunner       |
     * +---------+---------+
     * |
     * |
     * |                1  +---------------+
     * ^              +--->|withBeforeClass| setUpActionForFixture[i]
     * |              |    +---------------+
     * |              |
     * +------------------+ 1  n  +---------+---------+ 1  | n  +---------------+ forEachOracle; j
     * |ScriptiveUnit     +------>|GroupItemTestRunner|<>--+--->|MainAction     |   given/when/then[j]
     * |                  | 1     |                   |    |    +---------------+   (withTestCase[i])
     * |                  |<>-+   |groupId            |    |
     * +------------------+   |   +-------------------+    | 1  +---------------+
     * |   forEachTestCase; i       +--->|withAfterClass | tearDownActionForFixture[i]
     * |                                 +---------------+
     * |
     * |                              1  +---------------+
     * |                           +---->|withBeforeClass| setUpBeforeAll
     * |                           |     +---------------+
     * |                           |
     * +---------------------------+
     * |
     * |  1  +---------------+
     * +---->|withAfterClass | tearDownAfterAll
     * +---------------+
     *
     * ----
     * sequence:::
     *
     * ----
     * setUpBeforeAll (setUpFixture before given when (handleError|then) after) tearDownFixture)+ tearDownBeforeAll
     * ----
     */
    GROUP_BY_TEST_CASE {
      @Override
      public Iterable<Runner> createRunners(Session session, TestSuiteDescriptor testSuiteDescriptor) {
        return createRunnersGroupingByTestCase(session, testSuiteDescriptor);
      }
    },
    /**
     * byTestFixture::::
     * structure:::
     * [ditaa]
     * ----
     * +-------------------+
     * |ParentRunner       |
     * +---------+---------+
     * |
     * |
     * |                1  +---------------+
     * ^              +--->|withBeforeClass| setUpActionForFixture[i]
     * |              |    +---------------+
     * |              |
     * +------------------+ 1  n  +---------+---------+ 1  | n  +---------------+ forEachTestOracle; j
     * |ScriptiveUnit     +------>|GroupItemTestRunner|<>--+--->|MainAction     |   forEachTestCaseInFixture; k
     * |                  | 1     |                   |    |    +---------------+     given/when/then[j]
     * |                  |<>-+   |groupId            |    |                          (with testCase[k])
     * +------------------+   |   +-------------------+    | 1  +---------------+
     * |   forEachTestFixture; i    +--->|withAfterClass | tearDownActionForFixture[i]
     * |                                 +---------------+
     * |
     * |                              1  +---------------+
     * |                           +---->|withBeforeClass| setUpBeforeAll
     * |                           |     +---------------+
     * |                           |
     * +---------------------------+
     * |
     * |  1  +---------------+
     * +---->|withAfterClass | tearDownAfterAll
     * +---------------+
     * ----
     * sequence:::
     *
     * ----
     * setUpBeforeAll (setUpFixture before given when (handleError|then) after) tearDownFixture)+ tearDownBeforeAll
     * ----
     */
    GROUP_BY_TEST_FIXTURE {
      @Override
      public Iterable<Runner> createRunners(Session session, TestSuiteDescriptor testSuiteDescriptor) {
        return createRunnersGroupingByTestFixture(session, testSuiteDescriptor);
      }

      @Override
      public OrderBy orderBy() {
        return TEST_CASE;
      }
    },
    /**
     * This is similar to {@code Type.GROUP_BY_TEST_FIXTURE}, but concrete test oracles are ordered by test oracle definitions
     * first and then by test cases within a fixture.
     *
     * That is, main actions are constructed in a manner illustrated in the following diagram.
     *
     * [ditaa]
     * ----
     *
     * 1    n  +---------------+ forEachTestCaseInFixture; j
     * <>------>|MainAction     |   forEachTestOracle; k
     * +---------------+     given/when/then[k]
     * (with testCase[j])
     * ----
     */
    GROUP_BY_TEST_FIXTURE_ORDER_BY_TEST_ORACLE {
      @Override
      public Iterable<Runner> createRunners(Session session, TestSuiteDescriptor testSuiteDescriptor) {
        return createRunnersGroupingByTestFixture(session, testSuiteDescriptor);
      }

      @Override
      public OrderBy orderBy() {
        return TEST_ORACLE;
      }
    },
    ;

    public enum OrderBy {
      TEST_CASE {
        public Stream<Action> buildSortedActionStreamOrderingBy(Session session, List<IndexedTestCase> testCases, List<? extends TestOracle> testOracles) {
          return testCases.stream()
              .flatMap(eachTestCase -> testOracles
                  .stream()
                  .map((TestOracle eachOracle) ->
                      session.createMainActionForTestOracle(
                          eachOracle,
                          eachTestCase
                      )));
        }
      },
      TEST_ORACLE {
        @Override
        public Stream<Action> buildSortedActionStreamOrderingBy(Session session, List<IndexedTestCase> testCases, List<? extends TestOracle> testOracles) {
          return testOracles.stream()
              .flatMap(eachOracle -> testCases
                  .stream()
                  .map((IndexedTestCase eachTestCase) ->
                      session.createMainActionForTestOracle(
                          eachOracle,
                          eachTestCase
                      )));
        }
      };

      public abstract Stream<Action> buildSortedActionStreamOrderingBy(
          Session session,
          List<IndexedTestCase> testCases,
          List<? extends TestOracle> testOracles);
    }

    public abstract Iterable<Runner> createRunners(Session session, TestSuiteDescriptor testSuiteDescriptor);

    public OrderBy orderBy() {
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
  private GroupedTestItemRunner(
      Class<?> testClass,
      int groupId,
      Action beforeAction,
      List<Action> mainActions,
      Action afterAction) throws InitializationError {
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
        performActionWithLogging(beforeAction);
        super.evaluate();
      }
    };
  }

  @Override
  protected Statement withAfterClasses(Statement statement) {
    return new RunAfters(statement, Collections.emptyList(), null) {
      @Override
      public void evaluate() throws Throwable {
        super.evaluate();
        performActionWithLogging(afterAction);
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

  private static Statement actionBlock(Action action) {
    return actionInvoker(action);
  }

  private String testName(Action action) {
    return format("%s[%d]", action.toString(), this.groupId);
  }

  private static Statement actionInvoker(Action action) {
    return new Statement() {
      @Override
      public void evaluate() {
        performActionWithLogging(action);
      }
    };
  }

  private static GroupedTestItemRunner createRunnerForTestOracle(
      Class<?> testClass,
      int testOracleId,
      TestOracle testOracle,
      Session session,
      TestSuiteDescriptor testSuiteDescriptor) {
    try {
      return new GroupedTestItemRunner(
          testClass,
          testOracleId,
          nop(),
          ActionUtils.createMainActionsForTestOracle(
              session, testOracle,
              testSuiteDescriptor
          ),
          nop()
      );
    } catch (InitializationError initializationError) {
      throw ScriptiveUnitException.wrap(initializationError);
    }
  }

  private static GroupedTestItemRunner createRunnerForTestCase(
      Class<?> testClass,
      IndexedTestCase indexedTestCase,
      Session session,
      TestSuiteDescriptor testSuiteDescriptor) {
    int testCaseId = indexedTestCase.getIndex();
    try {
      Tuple testCaseTuple = indexedTestCase.get();
      Map<List<Object>, Object> memo = createMemo();
      return new GroupedTestItemRunner(
          testClass,
          testCaseId,
          session.createSetUpActionForFixture(
              testSuiteDescriptor,
              testCaseTuple
          ),
          createMainActionsForTestCase(
              session, indexedTestCase,
              testSuiteDescriptor.getTestOracles()),
          session.createTearDownActionForFixture(
              testSuiteDescriptor,
              testCaseTuple
          )
      );
    } catch (InitializationError initializationError) {
      throw ScriptiveUnitException.wrap(initializationError);
    }
  }

  private static GroupedTestItemRunner createRunnerForTestFixture(
      Class<?> testClass,
      int fixtureId,
      Tuple fixture,
      List<IndexedTestCase> testCasesFilteredByFixture,
      Session session,
      TestSuiteDescriptor testSuiteDescriptor) {
    try {
      return new GroupedTestItemRunner(
          testClass,
          fixtureId,
          session.createSetUpActionForFixture(testSuiteDescriptor, fixture),
          createMainActionsForTestFixture(testCasesFilteredByFixture, session, testSuiteDescriptor),
          session.createTearDownActionForFixture(testSuiteDescriptor, fixture)
      );
    } catch (InitializationError initializationError) {
      throw ScriptiveUnitException.wrap(initializationError);
    }
  }
}
