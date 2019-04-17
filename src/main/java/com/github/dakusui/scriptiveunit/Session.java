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
import static java.lang.String.format;

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

  ActionFactory actionFactoryForTestSuite();

  ActionFactory actionFactoryForFixture();

  ActionFactory actionFactoryForTestCase();

  ActionFactory actionFactoryForTestOracle();

  default Action createActionForTestOracle(
      TestOracle testOracle,
      String testSuiteDescription,
      IndexedTestCase input,
      TestSuiteDescriptor testSuiteDescriptor,
      ActionDescriptionComposer actionDescriptionComposer) {
    Stage.Type setup = SETUP;
    Stage.Type teardown = TEARDOWN;
    return sequential(
        actionDescriptionComposer.getActionName(),
        named(
            actionDescriptionComposer.getActionNameForFixtureSetup(),
            named(actionDescriptionComposer.getActionDescriptionForFixtureSetUp(),
                this.createFixtureLevelAction(
                    setup,
                    Session.StageFactory.fixtureLevel(
                        input.get(),
                        testSuiteDescriptor.statementFactory(),
                        this.getConfig()),
                    setup.getFixtureLevelActionFactory(testSuiteDescriptor)))

        ),
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
                named(
                    actionDescriptionComposer.getActionNameForFixtureTearDown(),
                    named(actionDescriptionComposer.getActionDescriptionForFixtureDescription(),
                        this.createFixtureLevelAction(
                            teardown,
                            Session.StageFactory.fixtureLevel(
                                input.get(),
                                testSuiteDescriptor.statementFactory(),
                                this.getConfig()),
                            teardown.getFixtureLevelActionFactory(testSuiteDescriptor)))
                ))
            .build()
    );
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
    Action fixtureLevelAction = createFixtureLevelAction(
        stageType,
        StageFactory.fixtureLevel(testCaseTuple, testSuiteDescriptor.statementFactory(), this.getConfig()),
        stageType.getFixtureLevelActionFactory(testSuiteDescriptor));
    return decorateFixtureLevelAction(
        actionName,
        fixtureLevelAction,
        tupleFormatter
    );
  }

  static Action decorateFixtureLevelAction(String actionName, Action fixtureLevelAction, Supplier<String> tupleFormatter) {
    return named(
        actionName,
        named(tupleFormatter.get(),
            fixtureLevelAction));
  }

  default Action createFixtureLevelAction(
      Stage.Type stageType,
      StageFactory stageFactory,
      Function<Stage, Action> actionFactory) {
    return actionFactory.apply(stageFactory.createStage(stageType));
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

    @Override
    public ActionFactory actionFactoryForTestSuite() {
      return null;
    }

    @Override
    public ActionFactory actionFactoryForFixture() {
      return null;
    }

    @Override
    public ActionFactory actionFactoryForTestCase() {
      return null;
    }

    @Override
    public ActionFactory actionFactoryForTestOracle() {
      return null;
    }
  }
}
