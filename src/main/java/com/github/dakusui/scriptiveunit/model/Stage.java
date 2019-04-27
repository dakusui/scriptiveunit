package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;

import static java.util.Objects.requireNonNull;

/**
 * A stage is a part of session, where various activities defined as Funcs are
 * executed.
 */
public interface Stage {
  Tuple getTestCaseTuple();

  <RESPONSE> RESPONSE response();

  /**
   * Reurns a type of this stage.
   *
   * @return Type of this stage.
   */
  Type getType();

  <T> T getArgument(int index);

  int sizeOfArguments();

  /**
   * Returns a throwable object which is thrown and captured in executions of {@code GIVEN},
   * {@code WHEN}, and {@code THEN} stages.
   * <p>
   * Calling this method in stages except for {@code FAILURE_HANDLING} will result in an
   * {@code IllegalStateException}.
   */
  Throwable getThrowable();

  Config getConfig();

  Report getReport();

  TestItem getTestItem();

  abstract class Delegating implements Stage {
    private final Stage target;

    protected Delegating(Stage stage) {
      this.target = requireNonNull(stage);
    }

    @Override
    public Tuple getTestCaseTuple() {
      return this.target.getTestCaseTuple();
    }

    @Override
    public <RESPONSE> RESPONSE response() {
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
    public Throwable getThrowable() {
      return this.target.getThrowable();
    }

    @Override
    public Config getConfig() {
      return this.target.getConfig();
    }

    @Override
    public Report getReport() {
      return this.target.getReport();
    }

    @Override
    public TestItem getTestItem() {
      return this.target.getTestItem();
    }
  }

  enum Type {
    CONSTRAINT_GENERATION,
    SETUP_BEFORE_ALL {
    },
    SETUP {
    },
    BEFORE,
    GIVEN,
    WHEN,
    THEN,
    FAILURE_HANDLING,
    AFTER,
    TEARDOWN {
    },
    TEARDOWN_AFTER_ALL {
    },
    ;
  }
}
