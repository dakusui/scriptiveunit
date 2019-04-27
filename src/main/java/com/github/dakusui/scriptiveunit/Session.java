package com.github.dakusui.scriptiveunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.Report;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestItem;
import com.github.dakusui.scriptiveunit.model.TestOracle;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.model.Stage.Type.CONSTRAINT_GENERATION;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.SETUP;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.TEARDOWN;

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
    return Stage.Factory.createFixtureLevelStage(CONSTRAINT_GENERATION, tuple, this.getConfig());
  }

  default Stage createSuiteLevelStage(Stage.Type type, Tuple commonFixture) {
    return Stage.Factory.createSuiteLevelStage(type, commonFixture, this.getConfig());
  }

  default Stage createOracleLevelStage(Stage.Type type, TestItem testItem, Report report) {
    return Stage.Factory.createOracleLevelStage(type, testItem, report, this.getConfig());
  }

  default <RESPONSE> Stage createOracleVerificationStage(TestItem testItem, RESPONSE response, Report report) {
    return Stage.Factory.createOracleVerificationStage(this, testItem, response, report);
  }

  default Stage createOracleFailureHandlingStage(TestItem testItem, Throwable throwable, Report report) {
    return Stage.Factory.createOracleFailureHandlingStage(this, testItem, throwable, report);
  }

  default Action createFixtureLevelActionForTestCase(
      Tuple testCaseTuple,
      Stage.Type stageType,
      Function<Stage, Action> fixtureLevelActionFactory) {
    return fixtureLevelActionFactory.apply(
        StageFactory.fixtureLevel(
            testCaseTuple,
            this.getConfig())
            .createStage(stageType));
  }

  interface StageFactory {
    Stage createStage(Stage.Type stageTyp);

    static StageFactory fixtureLevel(Tuple fixture, Config config) {
      return stageType -> Stage.Factory.createFixtureLevelStage(stageType, fixture, config);
    }
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
