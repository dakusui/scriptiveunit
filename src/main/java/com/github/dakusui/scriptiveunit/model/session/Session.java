package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Reporting;
import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.exceptions.Exceptions;
import com.github.dakusui.scriptiveunit.core.ScriptCompiler;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
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
    private final Script<?, ?, ?, ?>                                                script;
    private final Function<String, BiFunction<IndexedTestCase, TestOracle, Report>> reportCreator;
    private final TestSuiteDescriptor                                               testSuiteDescriptor;

    Impl(Script<?, ?, ?, ?> script, ScriptCompiler scriptCompiler) {
      this.script = script;
      Reporting reporting = this.script.getReporting()
          .orElseThrow(Exceptions::noReportingObjectIsAvailable);
      this.reportCreator = (scriptResourceName) -> (indexedTestCase, testOracle) -> Report.create(
          null,
          reporting.reportBaseDirectory,
          // Only name of a test script is wanted here.
          scriptResourceName,
          reporting.reportFileName,
          indexedTestCase,
          testOracle);
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
      TestOracleValuesFactory definition = testOracleActionFactory(
          tuple -> formatTestName(tuple, testSuiteDescriptor, testOracle.getDescription().orElse("noname")), indexedTestCase, testOracle);
      Tuple testCaseTuple = indexedTestCase.get();
      Report report = createReport(indexedTestCase, testOracle);
      return named(
          definition.describeTestCase(testCaseTuple),
          sequential(
              createBefore(definition, report, indexedTestCase, testOracle),
              attempt(ActionUtils.<Tuple, TestIO>test()
                  .given(createGiven(report, definition.givenFactory(), indexedTestCase, testOracle))
                  .when(createWhen(report, definition.whenFactory(), indexedTestCase, testOracle))
                  .then(createThen(report, definition.thenFactory(), indexedTestCase, testOracle)).build())
                  .recover(
                      AssertionError.class,
                      leaf(c -> createErrorHandler(definition, report, indexedTestCase, testOracle).accept(c.thrownException(), c)))
                  .ensure(createAfter(definition, report, indexedTestCase, testOracle))));
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

    Action createBefore(TestOracleValuesFactory testOracleValuesFactory, Report report, IndexedTestCase testCase, TestOracle testOracle) {
      Stage beforeStage = Stage.createOracleLevelStage(report, this.getScript(), testCase, testOracle);
      return testOracleValuesFactory.beforeFactory().apply(beforeStage);
    }

    Source<Tuple> createGiven(
        Report report,
        final Function<Stage, Matcher<Tuple>> stageMatcherFunction, IndexedTestCase testCase, TestOracle testOracle) {
      Stage givenStage = Stage.createOracleLevelStage(report, this.getScript(), testCase, testOracle);
      return context -> {
        Matcher<Tuple> matcher = stageMatcherFunction.apply(givenStage);
        assumeThat(testCase.get(), matcher);
        return testCase.get();
      };
    }

    Pipe<Tuple, TestIO> createWhen(Report report, final Function<Stage, Object> function, IndexedTestCase testCase, TestOracle testOracle) {
      return (Tuple testCaseTuple, Context context) -> {
        Stage whenStage = Stage.createOracleLevelStage(report, this.getScript(), testCase, testOracle);
        return TestIO.create(
            testCaseTuple,
            function.apply(whenStage));
      };
    }

    Sink<TestIO> createThen(Report report, Function<Stage, Function<Object, Matcher<Stage>>> matcherFunction, IndexedTestCase testCase, TestOracle testOracle) {
      return (testIO, context) -> {
        Stage thenStage = Impl.this.createOracleVerificationStage(testIO.getOutput(), report, testCase, testOracle);
        assertThat(
            format("Test:<%s> failed with input:<%s>",
                formatTestName(
                    testCase.get(),
                    testSuiteDescriptor,
                    testOracle.getDescription().orElse("(noname)")),
                testIO.getInput()),
            thenStage,
            matcherFunction.apply(thenStage).apply(testIO.getOutput()));
      };
    }

    Report createReport(IndexedTestCase testCase, TestOracle testOracle) {
      return this.reportCreator
          .apply(getScript().name())
          .apply(testCase, testOracle);
    }

    Sink<AssertionError> createErrorHandler(TestOracleValuesFactory definition, Report report, IndexedTestCase testCase, TestOracle testOracle) {
      return (input, context) -> {
        Stage onFailureStage = createOracleFailureHandlingStage(input, report, testCase, testOracle);
        definition.errorHandlerFactory().apply(onFailureStage);
        throw input;
      };
    }

    Action createAfter(TestOracleValuesFactory definition, Report report, IndexedTestCase testCase, TestOracle testOracle) {
      Stage afterStage = Stage.createOracleLevelStage(report, this.getScript(), testCase, testOracle);
      return definition.afterFactory().apply(afterStage);
    }

    <RESPONSE> Stage createOracleVerificationStage(RESPONSE response, Report report, IndexedTestCase testCase, TestOracle testOracle) {
      return Stage.Factory.oracleStageFor(
          getScript(),
          requireNonNull(response),
          testCase,
          testOracle,
          report,
          null
      );
    }

    Stage createOracleFailureHandlingStage(Throwable throwable, Report report, IndexedTestCase testCase, TestOracle testOracle) {
      return Stage.Factory.oracleStageFor(
          getScript(),
          null,
          testCase,
          testOracle,
          report,
          throwable
      );
    }

    static TestOracleValuesFactory testOracleActionFactory(Function<Tuple, String> testCaseFormatter, IndexedTestCase testCase, TestOracle testOracle) {
      return TestOracleValuesFactory.createTestOracleValuesFactory(
          testCaseFormatter, testCase, testOracle
      );
    }
  }
}
