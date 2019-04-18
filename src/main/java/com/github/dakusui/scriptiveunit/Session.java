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
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
        testSuiteDescriptor.getSetUpActionFactory(),
        testSuiteDescriptor.statementFactory());
  }

  default Action createTearDownActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple) {
    return createActionForFixture(
        TEARDOWN,
        fixtureTuple,
        testSuiteDescriptor.getTearDownActionFactory(),
        testSuiteDescriptor.statementFactory());
  }

  default Action createActionForFixture(
      Stage.Type fixtureLevelStageType,
      Tuple fixtureTuple,
      Function<Stage, Action> fixtureLevelActionFactory,
      Statement.Factory statementFactory) {
    return createFixtureLevelActionForTestCase(
        fixtureTuple,
        fixtureLevelStageType,
        fixtureLevelActionFactory,
        statementFactory);
  }

  default Stage createConstraintConstraintGenerationStage(Statement.Factory statementFactory, Tuple tuple) {
    return Stage.Factory.createConstraintGenerationStage(this.getConfig(), statementFactory, tuple);
  }

  default Stage createSuiteLevelStage(Stage.Type type, Tuple commonFixture, Statement.Factory statementFactory) {
    return Stage.Factory.createSuiteLevelStage(type, commonFixture, statementFactory, this.getConfig());
  }

  default Stage createOracleLevelStage(Stage.Type type, TestItem testItem, Report report, Statement.Factory statementFactory) {
    return Stage.Factory.createOracleLevelStage(type, testItem, report, statementFactory, this.getConfig());
  }

  default <RESPONSE> Stage createOracleVerificationStage(Statement.Factory statementFactory, TestItem testItem, RESPONSE response, Report report) {
    return Stage.Factory.createOracleVerificationStage(this, statementFactory, testItem, response, report);
  }

  default Stage createOracleFailureHandlingStage(TestItem testItem, Throwable throwable, Report report, Statement.Factory statementFactory) {
    return Stage.Factory.createOracleFailureHandlingStage(this, testItem, throwable, report, statementFactory);
  }

  default Action createFixtureLevelActionForTestCase(
      Tuple testCaseTuple,
      Stage.Type stageType,
      Function<Stage, Action> fixtureLevelActionFactory,
      Statement.Factory statementFactory) {
    return fixtureLevelActionFactory.apply(
        StageFactory.fixtureLevel(
            testCaseTuple,
            statementFactory,
            this.getConfig())
            .createStage(stageType));
  }

  interface StageFactory {
    Stage createStage(Stage.Type stageTyp);

    static StageFactory fixtureLevel(Tuple fixture, Statement.Factory statementFactory, Config config) {
      return stageType -> Stage.Factory.createFixtureLevelStage(stageType, fixture, statementFactory, config);
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
