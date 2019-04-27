package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.core.Config;

import java.util.Objects;

import static com.github.dakusui.scriptiveunit.core.Utils.checkState;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.CONSTRAINT_GENERATION;
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

  enum Factory {
    ;

    public static Stage createConstraintGenerationStage(Config config, Tuple tuple) {
      return createFixtureLevelStage(CONSTRAINT_GENERATION, tuple, config);
    }

    /**
     * Creates a suite level stage, which corresponds to {@code @}{@code BeforeClass} or {@code @}{@code AfterClass}.
     *
     * @param type          should be either SETUP_BEFORE_ALL or TEARDOWN_AFTER_ALL.
     * @param commonFixture A suite level settings.
     * @return Created stage.
     */
    public static Stage createSuiteLevelStage(
        Type type,
        Tuple commonFixture,
        Config config) {
      return _create(
          type,
          config,
          commonFixture,
          null,
          null,
          null,
          null);
    }

    /**
     * Creates a fixture level stage, which shares the same fixture specified by
     * {@code fixture} parameter.
     *
     * @param config  A config object
     * @param type    should be either SETUP or TEARDOWN
     * @param fixture A fixture level settings.
     * @return Created stage.
     */
    public static Stage createFixtureLevelStage(Type type, Tuple fixture, Config config) {
      return createSuiteLevelStage(type, fixture, config);
    }

    /**
     * Creates an oracle level stage.
     *
     * @param type     should be either GIVEN, WHEN, BEFORE, or AFTER
     * @param testItem An item to be executed as a test.
     * @param report   A report object to which tesing activities are written.
     * @param config   A config object
     * @return Created stage
     */
    public static Stage createOracleLevelStage(Type type, TestItem testItem, Report report, Config config) {
      return _create(type,
          config, testItem.getTestCaseTuple(), testItem, null, null, report);
    }

    public static <RESPONSE> Stage createOracleVerificationStage(Session session, TestItem testItem, RESPONSE response, Report report) {
      return _create(Type.THEN,
          session.getConfig(), testItem.getTestCaseTuple(), testItem,
          requireNonNull(response),
          null,
          report);
    }

    public static Stage createOracleFailureHandlingStage(Session session, TestItem testItem, Throwable throwable, Report report) {
      return _create(Type.FAILURE_HANDLING, session.getConfig(), testItem.getTestCaseTuple(), testItem, null, throwable, report);
    }

    private static <RESPONSE> Stage _create(Type type, Config config, Tuple testCase, TestItem testItem, RESPONSE response, Throwable throwable, Report report) {
      return new Stage() {
        @Override
        public Tuple getTestCaseTuple() {
          return testCase;
        }

        @SuppressWarnings("unchecked")
        @Override
        public RESPONSE response() {
          return checkState(response, Objects::nonNull);
        }

        @Override
        public Type getType() {
          return type;
        }

        @Override
        public <T> T getArgument(int index) {
          throw new UnsupportedOperationException();
        }

        @Override
        public int sizeOfArguments() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Throwable getThrowable() {
          return checkState(
              throwable,
              Objects::nonNull,
              "This method is only allowed to be called in '%s' stage but it was in '%s'", Type.FAILURE_HANDLING, this);
        }

        @Override
        public Config getConfig() {
          return config;
        }

        @Override
        public Report getReport() {
          return report;
        }

        @Override
        public TestItem getTestItem() {
          return checkState(
              testItem,
              Objects::nonNull,
              "This method is not allowed to be called in '%s' stage.", this
          );
        }
      };
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
