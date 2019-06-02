package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.GroupedTestItemRunner;
import com.github.dakusui.scriptiveunit.core.Config;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * A stage is a part of session, where various activities defined as Funcs are
 * executed.
 */
public interface Stage {
  /**
   * Reurns a type of this stage.
   *
   * @return Type of this stage.
   */
  Type getType();

  Config getConfig();

  int sizeOfArguments();

  <T> T getArgument(int index);

  Optional<Throwable> getThrowable();

  Optional<Tuple> getTestCaseTuple();

  <RESPONSE> Optional<RESPONSE> response();

  Optional<Report> getReport();

  Optional<TestItem> getTestItem();

  static Stage create(Type type, Config config, Tuple commonFixture) {
    return StageFactory._create2(
        type,
        config,
        commonFixture,
        null,
        null,
        null);
  }

  abstract class Delegating implements Stage {
    private final Stage target;

    protected Delegating(Stage stage) {
      this.target = requireNonNull(stage);
    }

    @Override
    public Optional<Tuple> getTestCaseTuple() {
      return this.target.getTestCaseTuple();
    }

    @Override
    public <RESPONSE> Optional<RESPONSE> response() {
      return this.target.response();
    }

    @Override
    public Type getType() {
      return this.target.getType();
    }

    @Override
    public <T> T getArgument(int index) {
      return this.target.getArgument(index);
    }

    @Override
    public int sizeOfArguments() {
      return this.target.sizeOfArguments();
    }

    @Override
    public Optional<Throwable> getThrowable() {
      return this.target.getThrowable();
    }

    @Override
    public Config getConfig() {
      return this.target.getConfig();
    }

    @Override
    public Optional<Report> getReport() {
      return this.target.getReport();
    }

    @Override
    public Optional<TestItem> getTestItem() {
      return this.target.getTestItem();
    }
  }

  enum Type {
    CONSTRAINT_GENERATION,
    SETUP_BEFORE_ALL,
    SETUP,
    BEFORE,
    GIVEN,
    WHEN,
    THEN,
    FAILURE_HANDLING,
    AFTER,
    TEARDOWN,
    TEARDOWN_AFTER_ALL,
    NONE
    ;
  }

  interface Factory {
    Stage suiteLevel(Tuple commonFixture);
    Stage fixtureLevel(Tuple fixture);
    Stage testCaseLevel(Tuple testCase);
    Stage oracleLevel(Tuple testCase);
  }

  enum Level {
    SUITE {
      @Override
      public List<Type> stageTypes(GroupedTestItemRunner.Type runnerType) {
        return asList(Type.CONSTRAINT_GENERATION, Type.SETUP_BEFORE_ALL, Type.TEARDOWN_AFTER_ALL);
      }
    },
    FIXTURE {
      @Override
      public List<Type> stageTypes(GroupedTestItemRunner.Type runnerType) {
        if (runnerType == GroupedTestItemRunner.Type.GROUP_BY_TEST_FIXTURE ||
            runnerType == GroupedTestItemRunner.Type.GROUP_BY_TEST_FIXTURE_ORDER_BY_TEST_ORACLE)
          return asList(Type.SETUP, Type.TEARDOWN);
        return emptyList();
      }
    },
    TESTCASE {
      @Override
      public List<Type> stageTypes(GroupedTestItemRunner.Type runnerType) {
        if (runnerType == GroupedTestItemRunner.Type.GROUP_BY_TEST_ORACLE)
          return asList(Type.SETUP, Type.BEFORE, Type.GIVEN, Type.WHEN, Type.THEN, Type.FAILURE_HANDLING, Type.AFTER,
              Type.TEARDOWN);
        if (runnerType == GroupedTestItemRunner.Type.GROUP_BY_TEST_CASE)
          return asList(Type.SETUP, Type.TEARDOWN);
        if (runnerType == GroupedTestItemRunner.Type.GROUP_BY_TEST_FIXTURE)
          return emptyList();
        if (runnerType == GroupedTestItemRunner.Type.GROUP_BY_TEST_FIXTURE_ORDER_BY_TEST_ORACLE)
          return asList(Type.BEFORE, Type.GIVEN, Type.WHEN, Type.THEN, Type.FAILURE_HANDLING, Type.AFTER);
        return emptyList();
      }
    },
    ORACLE {
      @Override
      public List<Type> stageTypes(GroupedTestItemRunner.Type runnerType) {
        if (runnerType == GroupedTestItemRunner.Type.GROUP_BY_TEST_ORACLE)
          return emptyList();
        if (runnerType == GroupedTestItemRunner.Type.GROUP_BY_TEST_CASE ||
            runnerType == GroupedTestItemRunner.Type.GROUP_BY_TEST_FIXTURE)
          return asList(Type.BEFORE, Type.GIVEN, Type.WHEN, Type.THEN, Type.FAILURE_HANDLING, Type.AFTER);
        return emptyList();
      }
    };

    public abstract List<Type> stageTypes(GroupedTestItemRunner.Type runnerType);
  }
}
