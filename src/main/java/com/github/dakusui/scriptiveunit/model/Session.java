package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.model.Stage.Type.CONSTRAINT_GENERATION;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.SETUP;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.TEARDOWN;
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

  default Action createMainActionForTestOracle(
      TestOracle testOracle,
      IndexedTestCase indexedTestCase,
      Map<List<Object>, Object> memo
  ) {
    return testOracle
        .createTestActionFactory(TestItem.create(indexedTestCase, testOracle), memo)
        .apply(this);
  }

  default Action createSetUpActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple) {
    return createActionForFixture(
        SETUP,
        fixtureTuple,
        testSuiteDescriptor.getSetUpActionFactory());
  }

  default Action createTearDownActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple) {
    return createActionForFixture(
        TEARDOWN,
        fixtureTuple,
        testSuiteDescriptor.getTearDownActionFactory());
  }

  default Action createActionForFixture(
      Stage.Type fixtureLevelStageType,
      Tuple fixtureTuple,
      Function<Stage, Action> fixtureLevelActionFactory) {
    return createFixtureLevelActionForTestCase(
        fixtureTuple,
        fixtureLevelStageType,
        fixtureLevelActionFactory
    );
  }

  default Stage createConstraintConstraintGenerationStage(Tuple tuple) {
    return StageFactory._create(
        CONSTRAINT_GENERATION,
        this.getConfig(),
        tuple,
        null,
        null,
        null,
        null);
  }

  default Stage createSuiteLevelStage(Stage.Type type, Tuple commonFixture) {
    return StageFactory._create(
        type,
        this.getConfig(),
        commonFixture,
        null,
        null,
        null,
        null);
  }

  default Stage createOracleLevelStage(Stage.Type type, TestItem testItem, Report report) {
    return StageFactory._create(type,
        this.getConfig(), testItem.getTestCaseTuple(), testItem, null, null, report);
  }

  default <RESPONSE> Stage createOracleVerificationStage(TestItem testItem, RESPONSE response, Report report) {
    return StageFactory._create(Stage.Type.THEN,
        getConfig(), testItem.getTestCaseTuple(), testItem,
        requireNonNull(response),
        null,
        report);
  }

  default Stage createOracleFailureHandlingStage(TestItem testItem, Throwable throwable, Report report) {
    return StageFactory._create(Stage.Type.FAILURE_HANDLING, getConfig(), testItem.getTestCaseTuple(), testItem, null, throwable, report);
  }

  default Action createFixtureLevelActionForTestCase(
      Tuple testCaseTuple,
      Stage.Type stageType,
      Function<Stage, Action> fixtureLevelActionFactory) {
    return fixtureLevelActionFactory.apply(StageFactory.createFixtureLevelStage(stageType, testCaseTuple, this.getConfig()));
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
