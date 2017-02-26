package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.Objects;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.core.Utils.checkState;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.CONSTRAINT_GENERATION;
import static java.util.Objects.requireNonNull;

public interface Stage {
  Statement.Factory getStatementFactory();

  Tuple getTestCaseTuple();

  <RESPONSE> RESPONSE response();

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
    public Statement.Factory getStatementFactory() {
      return this.target.getStatementFactory();
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
  }

  enum Factory {
    ;

    public static Stage createConstraintGenerationStage(Config config, Statement.Factory statementFactory, Tuple tuple) {
      return _create(CONSTRAINT_GENERATION, config, statementFactory, tuple, null, null, null);
    }

    public static Stage createTopLevel(Session session) {
      return _create(Type.TOPLEVEL, session.getConfig(), new Statement.Factory(session), new Tuple.Builder().build(), null, null, null);
    }

    public static Stage createSuiteLevelStage(Type type /* should be either SETUP_BEFORE_ALL or TEARDOWN_AFTER_ALL */, Session session, Tuple commonFixture) {
      return _create(type, session.getConfig(), new Statement.Factory(session), commonFixture, null, null, null);
    }

    public static Stage createFixtureLevelStage(Type type /* should be either SETUP or TEARDOWN */, Session session, Tuple fixture) {
      return _create(type, session.getConfig(), new Statement.Factory(session), fixture, null, null, null);
    }

    public static Stage createOracleLevelStage(Type type /* should be either GIVEN, WHEN, BEFORE, or AFTER */, Session session, Tuple testCase, Report report) {
      return _create(type, session.getConfig(), new Statement.Factory(session), testCase, null, null, report);
    }

    public static <RESPONSE> Stage createOracleVerificationStage(Session session, Tuple testCase, RESPONSE response, Report report) {
      return _create(Type.THEN, session.getConfig(), new Statement.Factory(session), testCase, requireNonNull(response), null, report);
    }

    public static Stage createOracleFailureHandlingStage(Session session, Tuple testCase, Throwable throwable, Report report) {
      return _create(Type.FAILURE_HANDLING, session.getConfig(), new Statement.Factory(session), testCase, null, throwable, report);
    }

    private static <RESPONSE> Stage _create(Type type, Config config, Statement.Factory statementFactory, Tuple fixture, RESPONSE response, Throwable throwable, Report report) {
      return new Stage() {
        @Override
        public Statement.Factory getStatementFactory() {
          return statementFactory;
        }

        @Override
        public Tuple getTestCaseTuple() {
          return fixture;
        }

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
          return null; //.testItem;
        }
      };
    }
  }

  enum Type {
    CONSTRAINT_GENERATION,
    TOPLEVEL,
    SETUP_BEFORE_ALL {
      @Override
      public Func<Action> getSuiteLevelActionFactory(Session session) {
        return session.getDescriptor().getSetUpBeforeAllActionFactory();
      }
    },
    SETUP {
      @Override
      public Function<Stage, Action> getFixtureLevelActionFactory(Session session) {
        return session.getDescriptor().getSetUpActionFactory();
      }
    },
    BEFORE,
    GIVEN,
    WHEN,
    THEN,
    FAILURE_HANDLING ,
    AFTER,
    TEARDOWN {
      @Override
      public Function<Stage, Action> getFixtureLevelActionFactory(Session session) {
        return session.getDescriptor().getTearDownActionFactory();
      }
    },
    TEARDOWN_AFTER_ALL {
      @Override
      public Function<Stage, Action> getSuiteLevelActionFactory(Session session) {
        return session.getDescriptor().getTearDownAfterAllActionFactory();
      }
    },;

    public Function<Stage, Action> getSuiteLevelActionFactory(Session session) {
      throw new UnsupportedOperationException();
    }

    public Function<Stage, Action> getFixtureLevelActionFactory(Session session) {
      throw new UnsupportedOperationException();
    }

    public <T extends AssertionError> Function<Stage, Sink<T>> getErrorHandlingSinkFactory(Session session) {
      throw new UnsupportedOperationException();
    }
  }
}
