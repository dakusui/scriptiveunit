package com.github.dakusui.scriptiveunit.action;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.scriptiveunit.ActionDescriptionComposer;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestItem;
import com.github.dakusui.scriptiveunit.model.TestOracle;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dakusui.actionunit.Actions.named;
import static com.github.dakusui.scriptiveunit.core.Utils.filterSimpleSingleLevelParametersOut;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.SETUP;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.TEARDOWN;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public enum ActionUtils {
  ;

  public static Action createSetUpActionForTestFixture(Tuple fixture, TestSuiteDescriptor testSuiteDescriptor, Session session) {
    Stage.Type setup = SETUP;
    Action fixtureLevelAction = Session.createFixtureLevelAction(
        setup,
        Session.StageFactory.fixtureLevel(fixture, testSuiteDescriptor.statementFactory(), session.getConfig()),
        setup.getFixtureLevelActionFactory(testSuiteDescriptor));
    return named(
        "Setup test fixture",
        named(format("fixture: %s", fixture),
            fixtureLevelAction));
  }

  public static Action createTearDownActionForTestFixture(
      Tuple fixture,
      TestSuiteDescriptor testSuiteDescriptor,
      Session session) {
    Stage.Type teardown = TEARDOWN;
    Action fixtureLevelAction = Session.createFixtureLevelAction(
        teardown,
        Session.StageFactory.fixtureLevel(fixture, testSuiteDescriptor.statementFactory(), session.getConfig()),
        teardown.getFixtureLevelActionFactory(testSuiteDescriptor));
    BiFunction<Tuple, Action, Action> tear_down_fixture = (fixture1, fixtureLevelAction1) ->
        named("Tear down fixture",
            named(format("fixture: %s", fixture1),
                fixtureLevelAction1));
    return tear_down_fixture.apply(fixture, fixtureLevelAction);
  }

  public static List<Action> createMainActionsForTestOracles(
      TestOracle testOracle,
      Session session,
      TestSuiteDescriptor testSuiteDescriptor) {
    List<Parameter> factors = testSuiteDescriptor.getFactorSpaceDescriptor().getParameters();
    String testSuiteDescription = testSuiteDescriptor.getDescription();
    List<IndexedTestCase> testCases = testSuiteDescriptor.getTestCases();
    return testCases.stream()
        .map(new Function<IndexedTestCase, Action>() {
          int i = 0;

          @Override
          public Action apply(IndexedTestCase input) {
            try {
              Tuple prettifiedTestCaseTuple = filterSimpleSingleLevelParametersOut(input.get(), factors);
              return session.createActionForTestOracle(
                  testOracle,
                  testSuiteDescription,
                  input,
                  testSuiteDescriptor,
                  new ActionDescriptionComposer(
                      format("%03d: %s", i, testOracle.templateDescription(input.get(), testSuiteDescription)),
                      format("%03d: Setup test fixture", i),
                      format("fixture: %s", prettifiedTestCaseTuple),
                      format("%03d: Tear down fixture", i),
                      format("fixture: %s", prettifiedTestCaseTuple)));
            } finally {
              i++;
            }
          }
        }).collect(toList());
  }

  public static List<Action> createMainActionsForTestCase(IndexedTestCase testCase, Session session, TestSuiteDescriptor testSuiteDescriptor, List<? extends TestOracle> testOracles, Tuple testCaseTuple, Map<List<Object>, Object> memo) {
    return testOracles.stream()
        .map((TestOracle input) -> input.createTestActionFactory(
            TestItem.create(
                testSuiteDescriptor.getDescription(),
                testCase,
                input,
                input.getIndex()),
            testCaseTuple,
            memo
        ).apply(session))
        .collect(toList());
  }

  public static List<Action> createMainActionsForTestFixture
      (List<IndexedTestCase> testCasesFilteredByFixture,
       Session session,
       TestSuiteDescriptor testSuiteDescriptor,
       AtomicInteger i) {
    return testSuiteDescriptor.getRunnerType()
        .orderBy()
        .buildSortedActionStreamOrderingBy(session, testCasesFilteredByFixture, i, testSuiteDescriptor)
        .collect(toList());
  }
}
