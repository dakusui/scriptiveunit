package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.stage.Stage;

import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.model.stage.Stage.ExecutionLevel.FIXTURE;
import static com.github.dakusui.scriptiveunit.model.stage.Stage.ExecutionLevel.SUITE;
import static java.util.Objects.requireNonNull;

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

  Report createReport(TestItem testItem);

  TestSuiteDescriptor getTestSuiteDescriptor();

  Action createSetUpActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple);

  Action createMainActionForTestOracle(TestOracle testOracle, IndexedTestCase indexedTestCase);

  Action createTearDownActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple);

  Action createSetUpBeforeAllAction(Tuple commonFixtureTuple);

  Action createTearDownAfterAllAction(Tuple commonFixtureTuple);

  default Stage createSuiteLevelStage(Tuple commonFixture) {
    return Stage.Factory.frameworkStageFor(SUITE, this.getConfig(), commonFixture);
  }

  default Stage createOracleLevelStage(TestItem testItem, Report report) {
    return Stage.Factory.oracleLevelStageFor(
        this.getConfig(),
        testItem,
        null,
        null,
        report);
  }

  default <RESPONSE> Stage createOracleVerificationStage(TestItem testItem, RESPONSE response, Report report) {
    return Stage.Factory.oracleLevelStageFor(
        getConfig(),
        testItem,
        requireNonNull(response),
        null,
        report);
  }

  default Stage createOracleFailureHandlingStage(TestItem testItem, Throwable throwable, Report report) {
    return Stage.Factory.oracleLevelStageFor(
        getConfig(),
        testItem,
        null,
        throwable,
        report);
  }

  static Session create(Config config, TestSuiteDescriptor.Loader testSuiteDescriptorLoader) {
    return new Impl(config, testSuiteDescriptorLoader);
  }

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
    public Report createReport(TestItem testItem) {
      return this.reportCreator.apply(testItem);
    }

    @Override
    public TestSuiteDescriptor getTestSuiteDescriptor() {
      return this.testSuiteDescriptor;
    }

    @Override
    public Action createMainActionForTestOracle(TestOracle testOracle, IndexedTestCase indexedTestCase) {
      return testOracle
          .createOracleActionFactory(TestItem.create(indexedTestCase, testOracle))
          .apply(this);
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

    Stage createFixtureLevelStage(Tuple testCaseTuple) {
      return Stage.Factory.frameworkStageFor(FIXTURE, this.getConfig(), testCaseTuple);
    }
  }
}
