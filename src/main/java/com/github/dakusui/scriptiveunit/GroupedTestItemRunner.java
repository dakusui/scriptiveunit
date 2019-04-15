package com.github.dakusui.scriptiveunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.scriptiveunit.action.ActionUtils;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.Actions.nop;
import static com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type.OrderBy.TEST_CASE;
import static com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type.OrderBy.TEST_ORACLE;
import static com.github.dakusui.scriptiveunit.action.ActionUtils.*;
import static com.github.dakusui.scriptiveunit.core.Utils.filterSimpleSingleLevelParametersOut;
import static com.github.dakusui.scriptiveunit.core.Utils.performActionWithLogging;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.SETUP;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.TEARDOWN;
import static com.github.dakusui.scriptiveunit.model.func.FuncInvoker.createMemo;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public final class GroupedTestItemRunner extends ParentRunner<Action> {
  private static Iterable<Runner> createRunnersGroupingByTestOracle(
      final Session session,
      TestSuiteDescriptor testSuiteDescriptor) {
    AtomicInteger id = new AtomicInteger(0);
    return testSuiteDescriptor.getTestOracles()
        .stream()
        .map(
            (Function<TestOracle, Runner>) input
                -> createRunnerForTestOracle(
                session.getConfig().getDriverObject().getClass(),
                id.getAndIncrement(),
                input,
                session,
                testSuiteDescriptor)
        ).collect(toList());
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
            fixtures.indexOf(fixture), fixture,
            testCases.stream().filter((IndexedTestCase indexedTestCase) -> project(indexedTestCase.get(), usedInSetUp).equals(fixture)).collect(toList()),
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
    return testCases.stream(
    ).map(
        (IndexedTestCase input) -> project(input.get(), involved)
    ).map(
        (Map<String, Object> input) -> new Tuple.Builder().putAll(input).build()
    ).distinct(
    ).collect(
        toCollection(LinkedList::new)
    );
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
    GROUP_BY_TEST_ORACLE {
      @Override
      public Iterable<Runner> createRunners(Session session, TestSuiteDescriptor testSuiteDescriptor) {
        return createRunnersGroupingByTestOracle(session, testSuiteDescriptor);
      }
    },
    GROUP_BY_TEST_CASE {
      @Override
      public Iterable<Runner> createRunners(Session session, TestSuiteDescriptor testSuiteDescriptor) {
        return createRunnersGroupingByTestCase(session, testSuiteDescriptor);
      }
    },
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
        public Stream<Action> buildSortedActionStreamOrderingBy(Session session, List<IndexedTestCase> testCases, AtomicInteger i, TestSuiteDescriptor testSuiteDescriptor) {
          List<? extends TestOracle> testOracles = testSuiteDescriptor.getTestOracles();
          return testCases.stream()
              .flatMap(eachTestCase -> {
                Map<List<Object>, Object> memo = createMemo();
                return testOracles.stream()
                    .map(eachOracle ->
                        eachOracle
                            .createTestActionFactory(
                                TestItem.create(testSuiteDescriptor.getDescription(), eachTestCase, eachOracle, i.getAndIncrement()),
                                eachTestCase.get(),
                                memo
                            ).apply(session)
                    );
              });
        }
      },
      TEST_ORACLE {
        @Override
        public Stream<Action> buildSortedActionStreamOrderingBy(Session session, List<IndexedTestCase> testCases, AtomicInteger i, TestSuiteDescriptor testSuiteDescriptor) {
          List<? extends TestOracle> testOracles = testSuiteDescriptor.getTestOracles();
          return testOracles.stream()
              .flatMap(eachOracle -> testCases.stream()
                  .map(eachTestCase ->
                      eachOracle.createTestActionFactory(
                          TestItem.create(testSuiteDescriptor.getDescription(), eachTestCase, eachOracle, i.getAndIncrement()),
                          eachTestCase.get(),
                          createMemo()
                      ).apply(session)));
        }
      };

      public abstract Stream<Action> buildSortedActionStreamOrderingBy(Session session, List<IndexedTestCase> testCases, AtomicInteger i, TestSuiteDescriptor testSuiteDescriptor);
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
      return new GroupedTestItemRunner(testClass,
          testOracleId,
          nop(),
          ActionUtils.createMainActionsForTestOracles(
              testOracle,
              session,
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
      IndexedTestCase testCase,
      Session session,
      TestSuiteDescriptor testSuiteDescriptor) {
    int testCaseId = testCase.getIndex();
    List<Parameter> parameters = testSuiteDescriptor.getFactorSpaceDescriptor().getParameters();
    List<? extends TestOracle> testOracles = testSuiteDescriptor.getTestOracles();
    try {
      AtomicInteger i = new AtomicInteger(0);
      Tuple testCaseTuple = testCase.get();
      Tuple prettifiedTestCaseTuple = filterSimpleSingleLevelParametersOut(testCase.get(), parameters);
      Map<List<Object>, Object> memo = createMemo();
      Supplier<String> stringSupplier = () -> format("fixture: %s", prettifiedTestCaseTuple);
      return new GroupedTestItemRunner(
          testClass,
          testCaseId,
          session.createFixtureLevelActionForTestCase(
              testCaseTuple,
              testSuiteDescriptor,
              format("%03d: Setup test fixture", i.getAndIncrement()),
              SETUP,
              stringSupplier),
          createMainActionsForTestCase(
              testCase,
              session,
              testSuiteDescriptor,
              testOracles,
              testCaseTuple,
              memo),
          session.createFixtureLevelActionForTestCase(
              testCaseTuple,
              testSuiteDescriptor,
              format("%03d: Teardown fixture", testOracles.size()), TEARDOWN, stringSupplier)
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
      AtomicInteger i = new AtomicInteger(0);
      return new GroupedTestItemRunner(
          testClass,
          fixtureId,
          createSetUpActionForTestFixture(fixture, testSuiteDescriptor, session),
          createMainActionsForTestFixture(testCasesFilteredByFixture, session, testSuiteDescriptor, i),
          createTearDownActionForTestFixture(fixture, testSuiteDescriptor, session)
      );
    } catch (InitializationError initializationError) {
      throw ScriptiveUnitException.wrap(initializationError);
    }
  }
}
