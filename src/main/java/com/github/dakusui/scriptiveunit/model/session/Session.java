package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.core.Reporting;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.model.desc.ConstraintDefinition;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.value.ValueUtils;
import com.github.dakusui.scriptiveunit.model.session.action.Pipe;
import com.github.dakusui.scriptiveunit.model.session.action.Sink;
import com.github.dakusui.scriptiveunit.model.session.action.Source;
import com.github.dakusui.scriptiveunit.utils.ActionUtils;
import org.hamcrest.Matcher;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.ActionSupport.attempt;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.ActionSupport.named;
import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static com.github.dakusui.scriptiveunit.utils.TestItemUtils.formatTestName;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public interface Session {

  Script getScript();

  TestSuiteDescriptor getTestSuiteDescriptor();

  Constraint createConstraint(ConstraintDefinition constraintDefinition);

  Action createSetUpBeforeAllAction(Tuple commonFixtureTuple);

  Action createSetUpActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple);

  Action createMainAction(TestOracle testOracle, IndexedTestCase indexedTestCase);

  Action createTearDownActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple);

  Action createTearDownAfterAllAction(Tuple commonFixtureTuple);

  static Session create(Script script, TestSuiteDescriptorLoader testSuiteDescriptorLoader) {
    return new Impl(script, testSuiteDescriptorLoader);
  }

  class Impl implements Session {
    private final Script                               script;
    private final BiFunction<TestItem, String, Report> reportCreator;
    private final TestSuiteDescriptor                  testSuiteDescriptor;

    @SuppressWarnings("WeakerAccess")
    protected Impl(Script script, TestSuiteDescriptorLoader testSuiteDescriptorLoader) {
      this.script = script;
      Reporting reporting = getScript()
          .getReporting()
          .orElseThrow(ScriptiveUnitException::noReportingObjectIsAvailable);
      this.reportCreator = (testItem, scriptResourceName) -> Report.create(
          null,
          reporting.reportBaseDirectory,
          // Only name of a test script is wanted here.
          scriptResourceName,
          testItem,
          reporting.reportFileName);
      this.testSuiteDescriptor = testSuiteDescriptorLoader.loadTestSuiteDescriptor(this);
    }

    @Override
    public Script getScript() {
      return this.script;
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
      return ActionSupport.named(
          format("Suite level set up: %s", testSuiteDescriptor.getDescription()),
          testSuiteDescriptor
              .setUpBeforeAll()
              .map(ValueUtils.INSTANCE::<Action>toValue)
              .map(f -> f.apply(this.createSuiteLevelStage(commonFixtureTuple)))
              .orElse(nop()));
    }

    @Override
    public Action createSetUpActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple) {
      return ActionSupport.named(
          "Fixture set up",
          testSuiteDescriptor
              .setUp()
              .map(ValueUtils.INSTANCE::<Action>toValue)
              .map(f -> f.apply(this.createFixtureLevelStage(fixtureTuple)))
              .orElse(nop()));
    }

    @Override
    public Action createMainAction(TestOracle testOracle, IndexedTestCase indexedTestCase) {
      TestItem testItem = TestItem.create(indexedTestCase, testOracle);
      TestOracleValuesFactory definition = testItem.testOracleActionFactory(
          tuple -> formatTestName(tuple, testSuiteDescriptor, testOracle.getDescription().orElse("noname")));
      Tuple testCaseTuple = testItem.getTestCaseTuple();
      Report report = createReport(testItem);
      return named(
          definition.describeTestCase(testCaseTuple),
          sequential(
              createBefore(testItem, definition, report),
              attempt(ActionUtils.<Tuple, TestIO>test()
                  .given(createGiven(testItem, report, definition.givenFactory()))
                  .when(createWhen(testItem, report, definition.whenFactory()))
                  .then(createThen(testItem, report, definition.thenFactory())).build())
                  .recover(
                      AssertionError.class,
                      leaf(c -> createErrorHandler(testItem, definition, report).accept(c.thrownException(), c)))
                  .ensure(createAfter(testItem, definition, report))));
    }

    @Override
    public Action createTearDownActionForFixture(TestSuiteDescriptor testSuiteDescriptor, Tuple fixtureTuple) {
      return ActionSupport.named(
          "Fixture tear down",
          testSuiteDescriptor
              .tearDown()
              .map(ValueUtils.INSTANCE::<Action>toValue)
              .map(f -> f.apply(this.createSuiteLevelStage(fixtureTuple)))
              .orElse(nop()));
    }

    @Override
    public Action createTearDownAfterAllAction(Tuple commonFixtureTuple) {
      return ActionSupport.named(
          format("Suite level tear down: %s", testSuiteDescriptor.getDescription()),
          testSuiteDescriptor
              .tearDownAfterAll()
              .map(ValueUtils.INSTANCE::<Action>toValue)
              .map(f -> f.apply(this.createSuiteLevelStage(commonFixtureTuple)))
              .orElse(nop()));
    }

    Action createBefore(TestItem testItem, TestOracleValuesFactory testOracleValuesFactory, Report report) {
      Stage beforeStage = this.createOracleLevelStage(testItem, report);
      return testOracleValuesFactory.beforeFactory().apply(beforeStage);
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
        Stage thenStage = Impl.this.createOracleVerificationStage(testItem, testIO.getOutput(), report);
        assertThat(
            format("Test:<%s> failed with input:<%s>",
                formatTestName(
                    testItem.getTestCaseTuple(),
                    testSuiteDescriptor,
                    testItem.getDescription().orElse("(noname)")),
                testIO.getInput()),
            thenStage,
            matcherFunction.apply(thenStage).apply(testIO.getOutput()));
      };
    }

    Report createReport(TestItem testItem) {
      return this.reportCreator.apply(
          testItem,
          getScript().name());
    }

    Sink<AssertionError> createErrorHandler(TestItem testItem, TestOracleValuesFactory definition, Report report) {
      return (input, context) -> {
        Stage onFailureStage = createOracleFailureHandlingStage(testItem, input, report);
        definition.errorHandlerFactory().apply(onFailureStage);
        throw input;
      };
    }

    Action createAfter(TestItem testItem, TestOracleValuesFactory definition, Report report) {
      Stage afterStage = this.createOracleLevelStage(testItem, report);
      return definition.afterFactory().apply(afterStage);
    }

    Stage createSuiteLevelStage(Tuple suiteLevelTuple) {
      return Stage.Factory.frameworkStageFor(this.getScript(), suiteLevelTuple);
    }

    Stage createFixtureLevelStage(Tuple fixtureLevelTuple) {
      return Stage.Factory.frameworkStageFor(this.getScript(), fixtureLevelTuple);
    }

    Stage createOracleLevelStage(TestItem testItem, Report report) {
      return Stage.Factory.oracleLevelStageFor(
          this.getScript(),
          testItem,
          null,
          null,
          report);
    }

    <RESPONSE> Stage createOracleVerificationStage(TestItem testItem, RESPONSE response, Report report) {
      return Stage.Factory.oracleLevelStageFor(
          getScript(),
          testItem,
          requireNonNull(response),
          null,
          report);
    }

    Stage createOracleFailureHandlingStage(TestItem testItem, Throwable throwable, Report report) {
      return Stage.Factory.oracleLevelStageFor(
          getScript(),
          testItem,
          null,
          throwable,
          report);
    }
  }
}
