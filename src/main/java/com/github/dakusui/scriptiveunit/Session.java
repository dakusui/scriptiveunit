package com.github.dakusui.scriptiveunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.*;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.github.dakusui.actionunit.Actions.named;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.SETUP;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.TEARDOWN;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public interface Session {
  Config getConfig();

  Report createReport(TestItem testItem);

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

  default Action createSetUpActionForTestCase(
      AtomicInteger i,
      Tuple testCaseTuple,
      Tuple prettifiedTestCaseTuple,
      TestSuiteDescriptor testSuiteDescriptor) {
    return named(
        format("%03d: Setup test fixture", i.getAndIncrement()),
        named(format("fixture: %s", prettifiedTestCaseTuple),
            requireNonNull(createFixtureLevelAction(SETUP, testCaseTuple, testSuiteDescriptor))));
  }

  default Action createTearDownActionForTestCase(List<? extends TestOracle> testOracles, Tuple testCaseTuple, Tuple prettifiedTestCaseTuple, TestSuiteDescriptor testSuiteDescriptor) {
    return named(
        format("%03d: Tear down fixture", testOracles.size()),
        named(format("fixture: %s", prettifiedTestCaseTuple),
            requireNonNull(createFixtureLevelAction(TEARDOWN, testCaseTuple, testSuiteDescriptor))
        )
    );
  }

  default Action createFixtureLevelAction(Stage.Type stageType, Tuple input, TestSuiteDescriptor testSuiteDescriptor) {
    return stageType
        .getFixtureLevelActionFactory(testSuiteDescriptor)
        .apply(
            Stage.Factory.createFixtureLevelStage(
                stageType,
                input,
                testSuiteDescriptor.statementFactory(),
                this.getConfig()));
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
