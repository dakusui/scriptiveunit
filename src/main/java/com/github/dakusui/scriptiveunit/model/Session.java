package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;

import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.model.Stage.ExecutionLevel.FIXTURE;
import static com.github.dakusui.scriptiveunit.model.Stage.ExecutionLevel.SUITE;
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

  default Action createMainActionForTestOracle(TestOracle testOracle, IndexedTestCase indexedTestCase) {
    return testOracle
        .createOracleActionFactory(TestItem.create(indexedTestCase, testOracle))
        .apply(this);
  }

  default Action createSetUpActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple) {
    return testSuiteDescriptor
        .getSetUpActionFactory()
        .apply(createFixtureLevelStage(fixtureTuple, FIXTURE));
  }

  default Action createTearDownActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple) {
    return testSuiteDescriptor
        .getTearDownActionFactory()
        .apply(createFixtureLevelStage(fixtureTuple, FIXTURE));
  }

  default Stage createSuiteLevelStage(Tuple commonFixture) {
    return StageFactory.frameworkStageFor(SUITE, this.getConfig(), commonFixture);
  }

  default Stage createFixtureLevelStage(Tuple testCaseTuple, Stage.ExecutionLevel stageExecutionLevel) {
    return StageFactory.frameworkStageFor(
        stageExecutionLevel,
        this.getConfig(),
        testCaseTuple
    );
  }

  default Stage createOracleLevelStage(Stage.ExecutionLevel executionLevel, TestItem testItem, Report report) {
    return StageFactory.oracleLevelStageFor(
        executionLevel,
        this.getConfig(),
        testItem,
        null,
        null,
        report);
  }

  default <RESPONSE> Stage createOracleVerificationStage(TestItem testItem, RESPONSE response, Report report) {
    return StageFactory.oracleLevelStageFor(
        Stage.ExecutionLevel.ORACLE,
        getConfig(),
        testItem,
        requireNonNull(response),
        null,
        report);
  }

  default Stage createOracleFailureHandlingStage(TestItem testItem, Throwable throwable, Report report) {
    return StageFactory.oracleLevelStageFor(
        Stage.ExecutionLevel.ORACLE,
        getConfig(),
        testItem,
        null,
        throwable,
        report);
  }

  static Session create(Config config) {
    return new Impl(config);
  }

  class Impl implements Session {
    private final Config                     config;
    private final Function<TestItem, Report> reportCreator;

    @SuppressWarnings("WeakerAccess")
    protected Impl(Config config) {
      this.config = config;
      this.reportCreator = testItem ->
          Report.create(
              testItem,
              getConfig().getScriptResourceName(),
              getConfig().getBaseDirectory(),
              getConfig().getReportFileName());
    }

    @Override
    public Config getConfig() {
      return this.config;
    }

    @Override
    public Report createReport(TestItem testItem) {
      return reportCreator.apply(testItem);
    }
  }
}
