package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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

  default Stage createChildOf(Type type) {
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

  default Stage createChildOf(Type type, Tuple tuple) {
    requireNonNull(tuple);
    return new Delegating(createChildOf(type)) {
      @Override
      public Optional<Tuple> getTestCaseTuple() {
        return Optional.of(tuple);
      }
    };
  }

  default Stage createChildOf(Type type, Function<Tuple, TestItem> testItemFunction, Report report) {
    requireNonNull(testItemFunction);
    requireNonNull(report);
    return new Delegating(createChildOf(type)) {
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
}
