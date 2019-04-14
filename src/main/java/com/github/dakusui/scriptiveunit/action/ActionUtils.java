package com.github.dakusui.scriptiveunit.action;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.core.Config;
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

  public static Action createSetUpActionForTestCase(AtomicInteger i, Tuple testCaseTuple, Tuple prettifiedTestCaseTuple, TestSuiteDescriptor testSuiteDescriptor, Config config) {
    return named(
        format("%03d: Setup test fixture", i.getAndIncrement()),
        named(format("fixture: %s", prettifiedTestCaseTuple),
            requireNonNull(createFixtureLevelAction(SETUP, testCaseTuple, testSuiteDescriptor, config))));
  }

  public static Action createSetUpActionForTestFixture(Tuple fixture, TestSuiteDescriptor testSuiteDescriptor, Config config) {
    return named(
        "Setup test fixture",
        named(format("fixture: %s", fixture),
            requireNonNull(createFixtureLevelAction(SETUP, fixture, testSuiteDescriptor, config))));
  }

  public static Action createTearDownActionForTestCase(List<? extends TestOracle> testOracles, Tuple testCaseTuple, Tuple prettifiedTestCaseTuple, TestSuiteDescriptor testSuiteDescriptor, Config config) {
    return named(
        format("%03d: Tear down fixture", testOracles.size()),
        named(format("fixture: %s", prettifiedTestCaseTuple),
            requireNonNull(createFixtureLevelAction(TEARDOWN, testCaseTuple, testSuiteDescriptor, config))
        )
    );
  }

  public static Action createTearDownActionForTestFixture(Tuple fixture, TestSuiteDescriptor testSuiteDescriptor, Config config) {
    return named("Tear down fixture",
        named(format("fixture: %s", fixture),
            requireNonNull(createFixtureLevelAction(TEARDOWN, fixture, testSuiteDescriptor, config))));
  }

  private static Action createFixtureLevelAction(Stage.Type stageType, Tuple input, TestSuiteDescriptor testSuiteDescriptor, Config config) {
    return stageType
        .getFixtureLevelActionFactory(testSuiteDescriptor)
        .apply(Stage.Factory.createFixtureLevelStage(stageType, input, testSuiteDescriptor.statementFactory(), config));
  }

  public static List<Action> createMainActionsForTestOracles(
      TestOracle testOracle,
      Session session,
      TestSuiteDescriptor testSuiteDescriptor,
      final Config config) {
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
                          requireNonNull(createFixtureLevelAction(SETUP, input.get(), testSuiteDescriptor, config))
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
                                  requireNonNull(createFixtureLevelAction(TEARDOWN, input.get(), testSuiteDescriptor, config))
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
