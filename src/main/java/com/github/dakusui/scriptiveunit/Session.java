package com.github.dakusui.scriptiveunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.*;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.SETUP;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.TEARDOWN;
import static com.github.dakusui.scriptiveunit.model.func.FuncInvoker.createMemo;

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
  interface ActionFactory {
    Action setUp();

    Action tearDown();
  }

  Config getConfig();

  Report createReport(TestItem testItem);

  default ActionFactory actionFactoryForTestSuite() {
    return null;
  }

  default ActionFactory actionFactoryForFixture() {
    return null;
  }

  default ActionFactory actionFactoryForTestCase() {
    return null;
  }

  default ActionFactory actionFactoryForTestOracle() {
    return null;
  }

  default Action createActionForTestOracle(
      TestOracle testOracle,
      String testSuiteDescription,
      IndexedTestCase input,
      TestSuiteDescriptor testSuiteDescriptor,
      ActionDescriptionComposer actionDescriptionComposer) {
    return sequential(
        actionDescriptionComposer.getActionName(),
        createSetUpActionForFixture(input, testSuiteDescriptor, actionDescriptionComposer),
        attempt(
            testOracle.createTestActionFactory(
                TestItem.create(
                    testSuiteDescription,
                    input,
                    testOracle,
                    input.getIndex()),
                input.get(),
                createMemo()).apply(this))
            .ensure(
                createTearDownActionForFixture(input, testSuiteDescriptor, actionDescriptionComposer))
            .build());
  }

  default Action createTearDownActionForFixture(IndexedTestCase input, TestSuiteDescriptor testSuiteDescriptor, ActionDescriptionComposer actionDescriptionComposer) {
    return createActionForFixture(
        input,
        TEARDOWN,
        actionDescriptionComposer.getActionDescriptionForFixtureDescription(),
        actionDescriptionComposer.getActionDescriptionForFixtureDescription(),
        TEARDOWN.getFixtureLevelActionFactory(testSuiteDescriptor),
        testSuiteDescriptor.statementFactory());
  }

  default Action createSetUpActionForFixture(IndexedTestCase input, TestSuiteDescriptor testSuiteDescriptor, ActionDescriptionComposer actionDescriptionComposer) {
    return createActionForFixture(
        input,
        SETUP,
        actionDescriptionComposer.getActionNameForFixtureSetup(),
        actionDescriptionComposer.getActionDescriptionForFixtureSetUp(),
        SETUP.getFixtureLevelActionFactory(testSuiteDescriptor),
        testSuiteDescriptor.statementFactory());
  }

  default Action createActionForFixture(
      IndexedTestCase input,
      Stage.Type fixtureLevelStageType,
      String actionNameForFixtureLevel,
      String actionDescriptionForFixtureLevel,
      Function<Stage, Action> fixtureLevelActionFactory,
      Statement.Factory statementFactory) {
    return named(
        actionNameForFixtureLevel,
        named(actionDescriptionForFixtureLevel,
            fixtureLevelActionFactory.apply(StageFactory.fixtureLevel(
                input.get(),
                statementFactory,
                this.getConfig()).createStage(fixtureLevelStageType))));
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
      TestSuiteDescriptor testSuiteDescriptor,
      String actionName,
      Stage.Type stageType,
      Supplier<String> tupleFormatter) {
    return decorateFixtureLevelAction(
        actionName,
        stageType.getFixtureLevelActionFactory(testSuiteDescriptor)
            .apply(StageFactory.fixtureLevel(
                testCaseTuple,
                testSuiteDescriptor.statementFactory(),
                this.getConfig())
                .createStage(stageType)),
        tupleFormatter
    );
  }

  static Action decorateFixtureLevelAction(String actionName, Action fixtureLevelAction, Supplier<String> tupleFormatter) {
    return named(
        actionName,
        named(tupleFormatter.get(),
            fixtureLevelAction));
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
    private final Config config;
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
