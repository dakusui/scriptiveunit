package com.github.dakusui.scriptiveunit.action;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.scriptiveunit.model.Session;
import com.github.dakusui.scriptiveunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.TestOracle;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.github.dakusui.actionunit.Actions.attempt;
import static com.github.dakusui.scriptiveunit.model.func.FuncInvoker.createMemo;
import static java.util.stream.Collectors.toList;

public enum ActionUtils {
  ;

  public static List<Action> createMainActionsForTestOracle(
      Session session,
      TestOracle testOracle,
      TestSuiteDescriptor testSuiteDescriptor) {
    return testSuiteDescriptor.getTestCases().stream()
        .map(new Function<IndexedTestCase, Action>() {
          String testSuiteDescription = testSuiteDescriptor.getDescription();
          int i = 0;

          @Override
          public Action apply(IndexedTestCase input) {
            try {
              return Actions.sequential(
                  String.format("%03d: %s", i, testOracle.templateDescription(input.get(), testSuiteDescription)),
                  session.createSetUpActionForFixture(testSuiteDescriptor, input.get()),
                  attempt(
                      session.createMainActionForTestOracle(
                          testOracle,
                          input,
                          createMemo()
                      ))
                      .ensure(
                          session.createTearDownActionForFixture(testSuiteDescriptor, input.get()))
                      .build());
            } finally {
              i++;
            }
          }
        }).collect(toList());
  }

  public static List<Action> createMainActionsForTestCase(
      Session session,
      IndexedTestCase indexedTestCase,
      List<? extends TestOracle> testOracles, Map<List<Object>, Object> memo) {
    return testOracles.stream()
        .map((TestOracle input) ->
            session.createMainActionForTestOracle(input, indexedTestCase, memo))
        .collect(toList());
  }

  public static List<Action> createMainActionsForTestFixture
      (List<IndexedTestCase> testCasesFilteredByFixture,
          Session session,
          TestSuiteDescriptor testSuiteDescriptor) {
    return testSuiteDescriptor
        .getRunnerType()
        .orderBy()
        .buildSortedActionStreamOrderingBy(session, testCasesFilteredByFixture, testSuiteDescriptor.getTestOracles())
        .collect(toList());
  }
}
