package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Reporting;
import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.session.action.Pipe;
import com.github.dakusui.scriptiveunit.model.session.action.Sink;
import com.github.dakusui.scriptiveunit.model.session.action.Source;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.utils.ActionUtils;
import org.hamcrest.Matcher;

import java.util.Optional;
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

  Action createSetUpBeforeAllAction();

  Action createSetUpActionForFixture(Tuple testCaseTuple);

  Action createMainAction(TestOracle testOracle, IndexedTestCase indexedTestCase);

  Action createTearDownActionForFixture(Tuple testCaseTuple);

  Action createTearDownAfterAllAction();

  static Session create(Script script, ScriptCompiler scriptCompiler) {
    return new Impl(script, scriptCompiler);
  }

  class Impl implements Session {
    private final Script<?, ?, ?, ?>                   script;
    private final BiFunction<TestItem, String, Report> reportCreator;
    private final TestSuiteDescriptor                  testSuiteDescriptor;

    Impl(Script<?, ?, ?, ?> script, ScriptCompiler scriptCompiler) {
      this.script = script;
      Reporting reporting = this.script.getReporting()
          .orElseThrow(ScriptiveUnitException::noReportingObjectIsAvailable);
      this.reportCreator = (testItem, scriptResourceName) -> Report.create(
          null,
          reporting.reportBaseDirectory,
          // Only name of a test script is wanted here.
          scriptResourceName,
          testItem,
          reporting.reportFileName);
      this.testSuiteDescriptor = scriptCompiler.compile(this);
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
    public Action createSetUpBeforeAllAction() {
      Tuple commonFixtureTuple = this.getTestSuiteDescriptor().createCommonFixture();
      Optional<Statement> statement = getTestSuiteDescriptor().setUpBeforeAll();
      return ActionSupport.named(
          format("Suite level set up: %s", testSuiteDescriptor.getDescription()),
          statement.isPresent() ?
              Statement.eval(statement.get(), Stage.createSuiteLevelStage(commonFixtureTuple, this.getScript())) :
              nop());
    }

    @Override
    public Action createSetUpActionForFixture(Tuple testCaseTuple) {
      Tuple fixtureTuple = this.testSuiteDescriptor.createFixtureTupleFrom(testCaseTuple);
      Optional<Statement> statement = this.testSuiteDescriptor.setUp();
      return ActionSupport.named(
          "Fixture set up",
          statement.isPresent() ?
              Statement.eval(statement.get(), Stage.createSuiteLevelStage(fixtureTuple, this.getScript())) :
              nop());
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
    public Action createTearDownActionForFixture(Tuple testCaseTuple) {
      Tuple fixtureTuple = testSuiteDescriptor.createFixtureTupleFrom(testCaseTuple);
      Optional<Statement> statement = testSuiteDescriptor.tearDown();
      return ActionSupport.named(
          "Fixture tear down",
          statement.isPresent() ?
              Statement.eval(statement.get(), Stage.createSuiteLevelStage(fixtureTuple, this.getScript())) :
              nop());
    }

    @Override
    public Action createTearDownAfterAllAction() {
      Tuple commonFixtureTuple = testSuiteDescriptor.createCommonFixture();
      Optional<Statement> statement = testSuiteDescriptor.tearDownAfterAll();
      return ActionSupport.named(
          format("Suite level tear down: %s", testSuiteDescriptor.getDescription()),
          statement.isPresent() ?
              Statement.eval(statement.get(), Stage.createSuiteLevelStage(commonFixtureTuple, this.getScript())) :
              nop());
    }

    Action createBefore(TestItem testItem, TestOracleValuesFactory testOracleValuesFactory, Report report) {
      Stage beforeStage = Stage.createOracleLevelStage(testItem, report, this.getScript());
      return testOracleValuesFactory.beforeFactory().apply(beforeStage);
    }

    Source<Tuple> createGiven(
        TestItem testItem,
        Report report,
        final Function<Stage, Matcher<Tuple>> stageMatcherFunction) {
      Tuple testCaseTuple = testItem.getTestCaseTuple();
      Stage givenStage = Stage.createOracleLevelStage(testItem, report, this.getScript());
      return context -> {
        Matcher<Tuple> matcher = stageMatcherFunction.apply(givenStage);
        assumeThat(testCaseTuple, matcher);
        return testCaseTuple;
      };
    }

    Pipe<Tuple, TestIO> createWhen(TestItem testItem, Report report, final Function<Stage, Object> function) {
      return (testCase, context) -> {
        Stage whenStage = Stage.createOracleLevelStage(testItem, report, this.getScript());
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
      Stage afterStage = Stage.createOracleLevelStage(testItem, report, this.getScript());
      return definition.afterFactory().apply(afterStage);
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
