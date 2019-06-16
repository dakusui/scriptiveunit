package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.ConstraintDefinition;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import org.hamcrest.Matcher;

import java.util.function.Function;

import static com.github.dakusui.actionunit.Actions.attempt;
import static com.github.dakusui.actionunit.Actions.sequential;
import static com.github.dakusui.scriptiveunit.model.session.Stage.ExecutionLevel.FIXTURE;
import static com.github.dakusui.scriptiveunit.model.session.Stage.ExecutionLevel.SUITE;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public interface Session {
  Config getConfig();

  TestSuiteDescriptor getTestSuiteDescriptor();

  Constraint createConstraint(ConstraintDefinition constraintDefinition);

  Action createSetUpBeforeAllAction(Tuple commonFixtureTuple);

  Action createSetUpActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple);

  Action createMainAction(TestOracle testOracle, IndexedTestCase indexedTestCase);

  Action createTearDownActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple);

  Action createTearDownAfterAllAction(Tuple commonFixtureTuple);

  static Session create(Config config, TestSuiteDescriptor.TestSuiteDescriptorLoader testSuiteDescriptorLoader) {
    return new Impl(config, testSuiteDescriptorLoader);
  }

  class Impl implements Session {
    private final Config                     config;
    private final Function<TestItem, Report> reportCreator;
    private final TestSuiteDescriptor        testSuiteDescriptor;

    @SuppressWarnings("WeakerAccess")
    protected Impl(Config config, TestSuiteDescriptor.TestSuiteDescriptorLoader testSuiteDescriptorLoader) {
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
    public Constraint createConstraint(ConstraintDefinition constraintDefinition) {
      return Constraint.create(
          in -> constraintDefinition.test(createSuiteLevelStage(in)),
          constraintDefinition.involvedParameterNames());
    }

    @Override
    public Action createSetUpBeforeAllAction(Tuple commonFixtureTuple) {
      return testSuiteDescriptor
          .getSetUpBeforeAllActionFactory()
          .apply(this.createSuiteLevelStage(commonFixtureTuple));
    }

    @Override
    public Action createSetUpActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple) {
      return testSuiteDescriptor
          .getSetUpActionFactory()
          .apply(createFixtureLevelStage(fixtureTuple));
    }

    @Override
    public Action createMainAction(TestOracle testOracle, IndexedTestCase indexedTestCase) {
      TestItem testItem = TestItem.create(indexedTestCase, testOracle);
      TestOracle.Definition definition = testItem.oracleDefinition();
      Tuple testCaseTuple = testItem.getTestCaseTuple();
      Report report = createReport(testItem);
      return sequential(
          definition.describeTestCase(testCaseTuple),
          createBefore(testItem, definition, report),
          attempt(Actions.<Tuple, TestIO>test()
              .given(createGiven(testItem, report, definition.givenFactory()))
              .when(createWhen(testItem, report, definition.whenFactory()))
              .then(createThen(testItem, report, definition.thenFactory())).build())
              .recover(
                  AssertionError.class,
                  createErrorHandler(testItem, definition, report))
              .ensure(createAfter(testItem, definition, report))
              .build()
      );
    }

    @Override
    public Action createTearDownActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple) {
      return testSuiteDescriptor
          .getTearDownActionFactory()
          .apply(createFixtureLevelStage(fixtureTuple));
    }

    @Override
    public Action createTearDownAfterAllAction(Tuple commonFixtureTuple) {
      return testSuiteDescriptor
          .getTearDownAfterAllActionFactory()
          .apply(this.createSuiteLevelStage(commonFixtureTuple));
    }

    Action createBefore(TestItem testItem, TestOracle.Definition definition, Report report) {
      Stage beforeStage = this.createOracleLevelStage(testItem, report);
      return definition.beforeFactory(testItem, report).apply(beforeStage);
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

    Sink<AssertionError> createErrorHandler(TestItem testItem, TestOracle.Definition definition, Report report) {
      return (input, context) -> {
        Stage onFailureStage = createOracleFailureHandlingStage(testItem, input, report);
        definition.errorHandlerFactory(testItem, report).apply(onFailureStage);
        throw input;
      };
    }

    Action createAfter(TestItem testItem, TestOracle.Definition definition, Report report) {
      Stage afterStage = this.createOracleLevelStage(testItem, report);
      return definition.afterFactory(testItem, report).apply(afterStage);
    }

    Stage createSuiteLevelStage(Tuple suiteLevelTuple) {
      return Stage.Factory.frameworkStageFor(SUITE, this.getConfig(), suiteLevelTuple);
    }

    Stage createFixtureLevelStage(Tuple fixtureLevelTuple) {
      return Stage.Factory.frameworkStageFor(FIXTURE, this.getConfig(), fixtureLevelTuple);
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
