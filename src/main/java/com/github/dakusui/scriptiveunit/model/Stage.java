package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.GroupedTestItemRunner;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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

  Map<Func.Call, Object> memo();

  default Optional<Stage> parent() {
    return Optional.empty();
  }

  default Stage createChild(Type type) {
    requireNonNull(type);
    Stage stage = this;
    return new Delegating(stage) {
      @Override
      public Type getType() {
        return type;
      }

      @Override
      public Optional<Stage> parent() {
        return Optional.of(stage);
      }
    };
  }

  default Stage createChild(Type type, Tuple tuple) {
    requireNonNull(tuple);
    return new Delegating(createChild(type)) {
      @Override
      public Optional<Tuple> getTestCaseTuple() {
        return Optional.of(tuple);
      }
    };
  }

  default Stage createChild(Type type, Function<Tuple, TestItem> testItemFunction, Report report) {
    requireNonNull(testItemFunction);
    requireNonNull(report);
    return new Delegating(createChild(type)) {
      TestItem testItem = testItemFunction.apply(getTestCaseTuple().orElseThrow(RuntimeException::new));

      @Override
      public Optional<TestItem> getTestItem() {
        return Optional.of(testItem);
      }

      @Override
      public Optional<Report> getReport() {
        return Optional.of(report);
      }
    };
  }

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
    public Map<Func.Call, Object> memo() {
      return this.target.memo();
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
    ;
  }

  enum Scenario {
    BY_TESTORACLE {

    },
    BY_TESTCASE {

    },
    BY_TESTFIXTURE {

    },
    BY_TESTFIXTURE_ORDERED_BY_TESTORACLE {

    }

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
