package com.github.dakusui.scriptiveunit.action;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestItem;
import com.github.dakusui.scriptiveunit.model.TestOracle;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.scriptiveunit.core.Utils.filterSimpleSingleLevelParametersOut;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.SETUP;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.TEARDOWN;
import static com.github.dakusui.scriptiveunit.model.func.FuncInvoker.createMemo;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public enum ActionUtils {
  ;

  public static Action createSetUpActionForTestFixture(Tuple fixture, TestSuiteDescriptor testSuiteDescriptor, Session session) {
    return named(
        "Setup test fixture",
        named(format("fixture: %s", fixture),
            requireNonNull(session.createFixtureLevelAction(SETUP, fixture, testSuiteDescriptor))));
  }

  public static Action createTearDownActionForTestFixture(
      Tuple fixture,
      TestSuiteDescriptor testSuiteDescriptor,
      Session session) {
    return named("Tear down fixture",
        named(format("fixture: %s", fixture),
            requireNonNull(session.createFixtureLevelAction(TEARDOWN, fixture, testSuiteDescriptor))));
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
              return sequential(
                  format("%03d: %s", i, testOracle.templateDescription(input.get(), testSuiteDescription)),
                  named(
                      format("%03d: Setup test fixture", i),
                      named(format("fixture: %s", prettifiedTestCaseTuple),
                          requireNonNull(session.createFixtureLevelAction(SETUP, input.get(), testSuiteDescriptor))
                      )
                  ),
                  attempt(
                      testOracle.createTestActionFactory(
                          TestItem.create(
                              testSuiteDescription,
                              input,
                              testOracle,
                              input.getIndex()),
                          input.get(),
                          createMemo()).apply(session))
                      .ensure(
                          named(
                              format("%03d: Tear down fixture", i),
                              named(format("fixture: %s", prettifiedTestCaseTuple),
                                  requireNonNull(session.createFixtureLevelAction(TEARDOWN, input.get(), testSuiteDescriptor))
                              )))
                      .build()
              );
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
