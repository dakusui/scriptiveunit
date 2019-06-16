package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import org.hamcrest.Matcher;

import java.util.function.Function;

import static com.github.dakusui.actionunit.Actions.attempt;
import static com.github.dakusui.actionunit.Actions.sequential;
import static com.github.dakusui.scriptiveunit.model.stage.Stage.ExecutionLevel.FIXTURE;
import static com.github.dakusui.scriptiveunit.model.stage.Stage.ExecutionLevel.SUITE;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

/**
 * <pre>
 *   * Fixture
 *   * Test case
 *   * Test oracle
 *   GROUP_BY_TEST_ORACLE:
 *       beforeAll
 *       afterAll
 *   GROUP_BY_TEST_CASE:
 *       beforeAll
 *       afterAll
 *   GROUP_BY_TEST_FIXTURE:
 *       beforeAll
 *       afterAll
 *   GROUP_BY_TEST_FIXTURE_ORDER_BY_TEST_ORACLE:
 *       beforeAll
 *       afterAll
 *
 * </pre>
 */
public interface Session {
  Config getConfig();

  TestSuiteDescriptor getTestSuiteDescriptor();

  Action createSetUpActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple);

  Action createMainAction(TestOracle testOracle, IndexedTestCase indexedTestCase);

  Action createTearDownActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple);

  Action createSetUpBeforeAllAction(Tuple commonFixtureTuple);

  Action createTearDownAfterAllAction(Tuple commonFixtureTuple);

  default Stage createSuiteLevelStage(Tuple commonFixture) {
    return Stage.Factory.frameworkStageFor(SUITE, this.getConfig(), commonFixture);
  }

  static Session create(Config config, TestSuiteDescriptor.Loader testSuiteDescriptorLoader) {
    return new Impl(config, testSuiteDescriptorLoader);
  }

  Action createOracleAction(TestItem testItem, TestOracle.Box box);

  class Impl implements Session {
    private final Config                     config;
    private final Function<TestItem, Report> reportCreator;
    private final TestSuiteDescriptor        testSuiteDescriptor;

    @SuppressWarnings("WeakerAccess")
    protected Impl(Config config, TestSuiteDescriptor.Loader testSuiteDescriptorLoader) {
      this.config = config;
      this.reportCreator = testItem ->
          Report.create(
              testItem,
              getConfig().getScriptResourceName(),
              getConfig().getBaseDirectory(),
              getConfig().getReportFileName());
      this.testSuiteDescriptor = testSuiteDescriptorLoader.loadTestSuiteDescriptor(this);
    }

    @Override
    public Config getConfig() {
      return this.config;
    }

    @Override
    public TestSuiteDescriptor getTestSuiteDescriptor() {
      return this.testSuiteDescriptor;
    }

    @Override
    public Action createMainAction(TestOracle testOracle, IndexedTestCase indexedTestCase) {
      TestItem testItem = TestItem.create(indexedTestCase, testOracle);
      TestOracle.Box box = testItem.createBox();
      Tuple testCaseTuple = testItem.getTestCaseTuple();
      Report report = createReport(testItem);
      return sequential(
          box.describeTestCase(testCaseTuple),
          createBefore(testItem, box, report),
          attempt(Actions.<Tuple, TestIO>test()
              .given(createGiven(testItem, report, box.givenFactory()))
              .when(createWhen(testItem, report, box.whenFactory()))
              .then(createThen(testItem, report, box.thenFactory())).build())
              .recover(
                  AssertionError.class,
                  createErrorHandler(testItem, box, report))
              .ensure(createAfter(testItem, box, report))
              .build()
      );
    }

    @Override
    public Action createSetUpActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple) {
      return testSuiteDescriptor
          .getSetUpActionFactory()
          .apply(createFixtureLevelStage(fixtureTuple));
    }

    @Override
    public Action createTearDownActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple) {
      return testSuiteDescriptor
          .getTearDownActionFactory()
          .apply(createFixtureLevelStage(fixtureTuple));
    }

    @Override
    public Action createSetUpBeforeAllAction(Tuple commonFixtureTuple) {
      return testSuiteDescriptor
          .getSetUpBeforeAllActionFactory()
          .apply(this.createSuiteLevelStage(commonFixtureTuple));
    }

    @Override
    public Action createTearDownAfterAllAction(Tuple commonFixtureTuple) {
      return testSuiteDescriptor
          .getTearDownAfterAllActionFactory()
          .apply(this.createSuiteLevelStage(commonFixtureTuple));
    }

    @Override
    public Action createOracleAction(TestItem testItem, TestOracle.Box box) {
      return null;
    }

    Action createBefore(TestItem testItem, TestOracle.Box box, Report report) {
      Stage beforeStage = this.createOracleLevelStage(testItem, report);
      return box.beforeFactory(testItem, report).apply(beforeStage);
    }

    Source<Tuple> createGiven(
        TestItem testItem,
        Report report, final Function<Stage, Matcher<Tuple>> stageMatcherFunction) {
      Tuple testCaseTuple = testItem.getTestCaseTuple();
      Stage givenStage = createOracleLevelStage(testItem, report);
      return context -> {
        Matcher<Tuple> matcher = stageMatcherFunction.apply(givenStage);
        assumeThat(testCaseTuple, matcher);
        return testCaseTuple;
      };
    }

    Pipe<Tuple, TestIO> createWhen(TestItem testItem, Report report, final Function<Stage, Object> function) {
      return (testCase, context) -> {
        Stage whenStage = createOracleLevelStage(testItem, report);
        return TestIO.create(
            testCase,
            function.apply(whenStage));
      };
    }

    Sink<TestIO> createThen(TestItem testItem, Report report, Function<Stage, Function<Object, Matcher<Stage>>> matcherFunction) {
      return (testIO, context) -> {
        Stage thenStage = createOracleVerificationStage(testItem, testIO.getOutput(), report);
        assertThat(thenStage, matcherFunction.apply(thenStage).apply(testIO.getOutput()));
      };
    }

    Report createReport(TestItem testItem) {
      return this.reportCreator.apply(testItem);
    }

    Sink<AssertionError> createErrorHandler(TestItem testItem, TestOracle.Box box, Report report) {
      return (input, context) -> {
        Stage onFailureStage = createOracleFailureHandlingStage(testItem, input, report);
        box.errorHandlerFactory(testItem, report).apply(onFailureStage);
        throw input;
      };
    }

    Action createAfter(TestItem testItem, TestOracle.Box box, Report report) {
      Stage afterStage = this.createOracleLevelStage(testItem, report);
      return box.afterFactory(testItem, report).apply(afterStage);
    }

    Stage createFixtureLevelStage(Tuple testCaseTuple) {
      return Stage.Factory.frameworkStageFor(FIXTURE, this.getConfig(), testCaseTuple);
    }


    Stage createOracleLevelStage(TestItem testItem, Report report) {
      return Stage.Factory.oracleLevelStageFor(
          this.getConfig(),
          testItem,
          null,
          null,
          report);
    }

    <RESPONSE> Stage createOracleVerificationStage(TestItem testItem, RESPONSE response, Report report) {
      return Stage.Factory.oracleLevelStageFor(
          getConfig(),
          testItem,
          requireNonNull(response),
          null,
          report);
    }

    Stage createOracleFailureHandlingStage(TestItem testItem, Throwable throwable, Report report) {
      return Stage.Factory.oracleLevelStageFor(
          getConfig(),
          testItem,
          null,
          throwable,
          report);
    }
  }
}
