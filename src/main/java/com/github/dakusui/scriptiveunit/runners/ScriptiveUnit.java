package com.github.dakusui.scriptiveunit.runners;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Description;
import com.github.dakusui.scriptiveunit.model.form.handle.ObjectMethod;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.session.Session;
import com.github.dakusui.scriptiveunit.utils.DriverUtils;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import com.github.dakusui.scriptiveunit.utils.TupleUtils;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.Utils.createTestClassMock;
import static com.github.dakusui.scriptiveunit.exceptions.ResourceException.functionNotFound;
import static com.github.dakusui.scriptiveunit.utils.ActionUtils.performActionWithLogging;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.stream.Collectors.toList;

/**
 * A createAction test runner class of ScriptiveUnit.
 */
public class ScriptiveUnit extends Parameterized {
  /**
   * Test runners each of which runs a test case represented by an action.
   */
  private final List<Runner> runners;
  private final Session      session;
  private       Tuple        commonFixture;

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @param klass A test class.
   */
  @SuppressWarnings("unused")
  public ScriptiveUnit(Class<?> klass) throws Throwable {
    this(klass, new Config.Builder(klass, System.getProperties()).build());
  }

  /**
   * A constructor for testing.
   *
   * @param klass  A test class
   * @param config A config object.
   */
  public ScriptiveUnit(Class<?> klass, Config config) throws Throwable {
    this(klass, createTestSuiteDescriptorLoader(config));
  }

  public ScriptiveUnit(Class<?> klass, TestSuiteDescriptorLoader loader) throws Throwable {
    super(klass);
    this.session = Session.create(loader.getConfig(), loader);
    this.runners = newLinkedList(createRunners());
    this.commonFixture = TupleUtils.createCommonFixture(getTestSuiteDescriptor().getFactorSpaceDescriptor().getParameters());
  }

  @Override
  public String getName() {
    return this.session.getConfig().getScriptResourceName()
        .replaceAll(".+/", "")
        .replaceAll("\\.[^.]*$", "")
        + ":" + getTestSuiteDescriptor().getDescription();
  }


  @Override
  public List<Runner> getChildren() {
    return this.runners;
  }

  @Override
  protected TestClass createTestClass(Class<?> testClass) {
    return createTestClassMock(super.createTestClass(testClass));
  }

  @Override
  protected Statement withBeforeClasses(Statement statement) {
    return new RunBefores(statement, Collections.emptyList(), null) {
      @Override
      public void evaluate() throws Throwable {
        performActionWithLogging(session.createSetUpBeforeAllAction(commonFixture));
        super.evaluate();
      }
    };
  }

  @Override
  protected Statement withAfterClasses(Statement statement) {
    return new RunBefores(statement, Collections.emptyList(), null) {
      @Override
      public void evaluate() throws Throwable {
        super.evaluate();
        performActionWithLogging(session.createTearDownAfterAllAction(commonFixture));
      }
    };
  }

  private static TestSuiteDescriptorLoader createTestSuiteDescriptorLoader(Config config) {
    return TestSuiteDescriptorLoader.createInstance(
        ReflectionUtils.getAnnotationWithDefault(
            config.getDriverObject().getClass(),
            Load.DEFAULT_INSTANCE
        ).with(),
        config
    );
  }

  public Description describeFunction(Object driverObject, String functionName) {
    Optional<Description> value =
        Stream.concat(
            DriverUtils.getObjectMethodsFromImportedFieldsInObject(driverObject).stream().map(ObjectMethod::describe),
            getUserDefinedFormClauses().entrySet().stream().map((Map.Entry<String, List<Object>> entry) -> Description.describe(entry.getKey(), entry.getValue()))
        ).filter(t -> functionName.equals(t.name())).findFirst();
    if (value.isPresent())
      return value.get();
    throw functionNotFound(functionName);
  }

  public List<String> getFormNames(Object driverObject) {
    return Stream.concat(
        DriverUtils.getObjectMethodsFromImportedFieldsInObject(driverObject)
            .stream()
            .map(ObjectMethod::getName),
        getUserDefinedFormClauseNamesFromScript().stream()).collect(toList());
  }


  private List<String> getUserDefinedFormClauseNamesFromScript() {
    return new ArrayList<>(getUserDefinedFormClauses().keySet());
  }

  private Map<String, List<Object>> getUserDefinedFormClauses() {
    return getTestSuiteDescriptor().getUserDefinedFormClauses();
  }

  private Iterable<Runner> createRunners() {
    return getTestSuiteDescriptor().getRunnerMode().createRunners(this.session);
  }

  private TestSuiteDescriptor getTestSuiteDescriptor() {
    return this.session.getTestSuiteDescriptor();
  }

  /**
   * An {@code Enum} that provides definitions of {@code ScriptiviUnit}'s execution
   * modes.
   *
   * Here are definitions that are used in explanations of those execution modes.
   *
   * Test Case, Test Fixture, and Common Fixture::
   * In {@code Scriptive Unit}, A test suite consists of test cases and by executing
   * all test all oracles for each test cases, the entire suite will be executed.
   *
   * One test case is a tuple, which is a {@link Map} whose key is a {@link String}.
   * We can think of a test suite where some keys of all the test cases have the
   * constant values for all the test cases in the suite.
   *
   * Such entries are called a common fixture and they can be accessed from suite
   * level actions, which are {@code setUpBeforeAll} and {@code tearDownAfterAll}.
   *
   * A test fixture is implicitly defined by a user.
   * In {@code setUp} and {@code tearDown} actions, a user may access some values
   * of a test case.
   * The keys that the user used to access them will be a part of a test fixture.
   *
   * Following is a diagram that illustrates the relationships between test cases,
   * test fixtures, and the common fixture.
   *
   * //<pre>
   * [ditaa]
   * ----
   *
   *               Test Case
   *             |<-------------------------------------------------->|
   *
   *               Test Fixture
   *             |<------------------------>|
   *
   *               Common Fixture
   *             |<------->|
   *
   *             +---------+----------------+-------------------------+
   * Test Case[1]|         |                |                         |
   * Test Case[2]|         |                |
   * Test Case[3]|         |                                          |
   *             |                          |                         :
   *             |         |                :
   *             |         :
   *
   * ----
   * //</pre>
   *
   *
   * `fixtureOf(TestCase testCase)`::
   * A function that returns a test fixture from a given test case by projecting
   * only a related portion of it.
   *
   * `exercise(TestOracle testOracle, TestCase testCase)`::
   *
   * A function that exercise a given test oracle with a given test case.
   * Following pseudo code snippet illustrates a control flow of it.
   *
   * Note that this snippet is only for illustration and {@code ScriptiveUnit}
   * constructs a tree of actions that models this flow first and execute it.
   *
   * //<pre>
   * [source,java]
   * ----
   *
   * void exercise(TestOracle testOracle, TestCase testCase) {
   *   testOracle.before(testCase);
   *   try {
   *       if (testOracle.given(testCase))
   *         testOracle.then(testCase, testOracle.when(testCase));
   *       else
   *         throw new TestIgnored();
   *   } catch (Failure f) {
   *       throw testOracle.handleFailure(f, testCase);
   *   } finally {
   *     testOracle.after(testCase);
   *   }
   * }
   * ----
   * //</pre>
   *
   * `setUp` and `tearDown`::
   * `setUp` and `tearDown` are actions created from a test fixture.
   */
  public enum Mode {
    /**
     * A mode where test executions are grouped by test oracles first.
     *
     * structure::
     *
     * //<pre>
     * [ditaa]
     * ----
     *                            +-------------------+
     *                            |ParentRunner       |
     *                            +---------+---------+
     *                                      |
     *                                      |
     *                                      |                1  +---------------+
     *                                      ^              +--->|withBeforeClass| (nop)
     *                                      |              |    +---------------+
     *                                      |              |                      forEachTestCase; j
     * +------------------+ 1  n  +---------+---------+ 1  | n  +---------------+   setUp[j]
     * |ScriptiveUnit     +------>|GroupItemTestRunner|<>--+--->|MainActions    |   exercise(i, j)
     * |                  | 1     |                   |    |    +---------------+   tearDown[j]
     * |                  |<>-+   |groupId            |    |
     * +------------------+   |   +-------------------+    | 1  +---------------+
     *                        |   forEachTestOracle; i     +--->|withAfterClass | (nop)
     *                        |                                 +---------------+
     *                        |
     *                        |                              1  +---------------+
     *                        |                           +---->|withBeforeClass| setUpBeforeAll
     *                        |                           |     +---------------+
     *                        |                           |
     *                        +---------------------------+
     *                                                    |
     *                                                    |  1  +---------------+
     *                                                    +---->|withAfterClass | tearDownAfterAll
     *                                                          +---------------+
     *
     * ----
     * //</pre>
     *
     * sequence::
     *
     * //<pre>
     * [java]
     * ----
     * setUpBeforeAll(commonFixture)
     * try {
     *   for (TestOracle eachTestOracle: allTestOracles) {
     *     for (TestCase eachTestCase: allTestCases) {
     *       setUp(fixtureOf(eachTestCase));
     *       try {
     *         exercise(eachTestOracle, eachTestCase);
     *       } finally {
     *         tearDown(fixtureOf(eachTestCase));
     *       }
     *     }
     *   }
     * } finally {
     *   tearDownAfterAll(commonFixture)
     * }
     * ----
     * //</pre>
     */
    GROUP_BY_TEST_ORACLE {
      @Override
      public Iterable<Runner> createRunners(Session session) {
        return GroupedTestItemRunner.createRunnersGroupingByTestOracle(session);
      }
    },
    /**
     * A mode where test executions are grouped by test cases first.
     *
     * structure::
     *
     * //<pre>
     * [ditaa]
     * ----
     *                             +-------------------+
     *                             |ParentRunner       |
     *                             +---------+---------+
     *                                       |
     *                                       |
     *                                       |                 1  +---------------+
     *                                       ^               +--->|withBeforeClass| setUp[i]
     *                                       |               |    +---------------+
     *                                       |               |
     * +------------------+ 1  n  +----------+----------+ 1  | n  +---------------+ forEachOracle; j
     * |ScriptiveUnit     +------>|GroupedItemTestRunner|<>--+--->|MainAction     |   exercise(j, i);
     * |                  | 1     |                     |    |    +---------------+
     * |                  |<>-+   |groupId              |    |
     * +------------------+   |   +---------------------+    | 1  +---------------+
     *                        |   forEachTestCase; i         +--->|withAfterClass | tearDown[i]
     *                        |                                   +---------------+
     *                        |
     *                        |                                1  +---------------+
     *                        |                             +---->|withBeforeClass| setUpBeforeAll
     *                        |                             |     +---------------+
     *                        |                             |
     *                        +-----------------------------+
     *                                                      |
     *                                                      |  1  +---------------+
     *                                                      +---->|withAfterClass | tearDownAfterAll
     *                                                            +---------------+
     *
     * ----
     * //</pre>
     *
     * sequence::
     * //<pre>
     * [java]
     * ----
     * setUpBeforeAll(commonFixture)
     *   try {
     *     for (TestCase eachTestCase: allTestCases) {
     *       for (TestOracle eachTestOracle: allTestOracles) {
     *         setUp(fixtureOf(eachTestCase));
     *         try {
     *           exercise(eachTestOracle, eachTestCase);
     *         } finally {
     *           tearDown(fixtureOf(eachTestCase));
     *         }
     *       }
     *     }
     *   } finally {
     *     tearDownAfterAll(commonFixture)
     *   }
     * ----
     * //</pre>
     *
     * Note that this snippet is only for illustration and {@code ScriptiveUnit}
     * constructs a tree of actions that models this flow first and execute it.
     */
    GROUP_BY_TEST_CASE {
      @Override
      public Iterable<Runner> createRunners(Session session) {
        return GroupedTestItemRunner.createRunnersGroupingByTestCase(session);
      }
    },
    /**
     * A mode where test executions are grouped by test fixtures first.
     *
     * In case you know that your oracles do not modify your SUT (i.e. they are
     * repeatable) and it is expensive and necessary to build your SUT based on
     * test fixtures, using this mode would make a sense.
     *
     * In this mode, `setUp` and `tearDown` are performed only once for each test
     * fixture. For each test case that belongs to the test fixture, all oracles
     * are exercised.
     *
     * structure::
     *
     * //<pre>
     * [ditaa]
     * ----
     *                             +-------------------+
     *                             |ParentRunner       |
     *                             +---------+---------+
     *                                       |
     *                                       |
     *                                       |                 1  +---------------+
     *                                       ^               +--->|withBeforeClass| setUp[i]
     *                                       |               |    +---------------+
     *                                       |               |
     * +------------------+ 1  n  +----------+----------+ 1  | n  +---------------+ forEachTestCaseInFixture; j
     * |ScriptiveUnit     +------>|GroupedItemTestRunner|<>--+--->|MainAction     |   forEachTestOracle; k
     * |                  | 1     |                     |    |    +---------------+     exercise(k, j);
     * |                  |<>-+   |groupId              |    |
     * +------------------+   |   +---------------------+    | 1  +---------------+
     *                        |   forEachTestFixture; i      +--->|withAfterClass | tearDown[i]
     *                        |                                   +---------------+
     *                        |
     *                        |                                1  +---------------+
     *                        |                             +---->|withBeforeClass| setUpBeforeAll
     *                        |                             |     +---------------+
     *                        |                             |
     *                        +-----------------------------+
     *                                                      |
     *                                                      |  1  +---------------+
     *                                                      +---->|withAfterClass | tearDownAfterAll
     *                                                            +---------------+
     * ----
     * //</pre>
     *
     * sequence::
     *
     * //<pre>
     * [java]
     * ----
     * setUpBeforeAll(commonFixture)
     * try {
     *   for (TestFixture eachTestFixture: allTestFixtures) {
     *     setUp(eachTestFixture);
     *     try {
     *       for (TestCase eachTestCase: testCasesFor(eachTestFixture)) {
     *         for (TestOracle eachTestOracle: allTestOracles) {
     *           exercise(eachTestOracle, eachTestCase);
     *         }
     *       }
     *     } finally {
     *       tearDown(eachTestFixture);
     *     }
     *   }
     * } finally {
     *   tearDownAfterAll(commonFixture)
     * }
     * ----
     * //</pre>
     *
     * Note that this snippet is only for illustration and {@code ScriptiveUnit}
     * constructs a tree of actions that models this flow first and execute it.
     */
    GROUP_BY_TEST_FIXTURE {
      @Override
      public Iterable<Runner> createRunners(Session session) {
        return GroupedTestItemRunner.createRunnersGroupingByTestFixture(session);
      }

      @Override
      public OrderBy orderBy() {
        return OrderBy.TEST_CASE;
      }
    },
    /**
     * A mode where test executions are grouped by test fixtures first and then ordered by
     * test oracles.
     *
     * This is similar to {@code Type.GROUP_BY_TEST_FIXTURE}, but concrete test oracles are ordered by test oracle definitions
     * first and then by test cases within a fixture.
     *
     * That is, main actions are constructed in a manner illustrated in the following diagram.
     * //<pre>
     * [ditaa]
     * ----
     *
     *  1    n  +---------------+ forEachTestOracle; j
     * <>------>|MainAction     |   forEachTestCaseInFixture; k
     *          +---------------+     exercise(j, k)
     *          (with testCase[j])
     * ----
     * //</pre>
     *
     * sequence::
     * //<pre>
     * [java]
     * ----
     * setUpBeforeAll(commonFixture)
     * try {
     *   for (TestFixture eachTestFixture: allTestFixtures) {
     *     setUp(eachTestFixture);
     *     try {
     *       for (TestOracle eachTestOracle: allTestOracles) {
     *         for (TestCase eachTestCase: testCasesFor(eachTestFixture)) {
     *           exercise(eachTestOracle, eachTestCase);
     *         }
     *       }
     *     } finally {
     *       tearDown(eachTestFixture);
     *     }
     *   }
     * } finally {
     *   tearDownAfterAll(commonFixture)
     * }
     * ----
     * //</pre>
     *
     * Note that this snippet is only for illustration and {@code ScriptiveUnit}
     * constructs a tree of actions that models this flow first and execute it.
     */
    GROUP_BY_TEST_FIXTURE_ORDER_BY_TEST_ORACLE {
      @Override
      public Iterable<Runner> createRunners(Session session) {
        return GroupedTestItemRunner.createRunnersGroupingByTestFixture(session);
      }

      @Override
      public OrderBy orderBy() {
        return OrderBy.TEST_ORACLE;
      }
    },
    ;

    public enum OrderBy {
      TEST_CASE {
        public Stream<Action> buildSortedActionStreamOrderingBy(Session session, List<IndexedTestCase> testCases, List<? extends TestOracle> testOracles) {
          return testCases.stream()
              .flatMap(eachTestCase -> testOracles
                  .stream()
                  .map((TestOracle eachOracle) -> session.createMainAction(
                      eachOracle,
                      eachTestCase
                  )));
        }
      },
      TEST_ORACLE {
        @Override
        public Stream<Action> buildSortedActionStreamOrderingBy(Session session, List<IndexedTestCase> testCases, List<? extends TestOracle> testOracles) {
          return testOracles.stream()
              .flatMap((TestOracle eachOracle) -> testCases
                  .stream()
                  .map((IndexedTestCase eachTestCase) -> session.createMainAction(
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

    public abstract Iterable<Runner> createRunners(Session session);

    public OrderBy orderBy() {
      throw new UnsupportedOperationException();
    }
  }
}
