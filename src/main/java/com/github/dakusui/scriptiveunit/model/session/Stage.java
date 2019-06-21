package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormRegistry;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.Optional;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.indexOutOfBounds;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;
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
  ExecutionLevel getExecutionLevel();

  Config getConfig();

  int sizeOfArguments();

  <T> T getArgument(int index);

  Optional<Throwable> getThrowable();

  Optional<Tuple> getTestCaseTuple();

  <RESPONSE> Optional<RESPONSE> response();

  Optional<Report> getReport();

  Optional<TestItem> getTestItem();

  default FormRegistry formRegistry() {
    throw new UnsupportedOperationException();
  }

  default Statement.Nested ongoingStatement() {
    throw new UnsupportedOperationException();
  }

  default Stage createChild(Statement.Nested statement) {
    requireNonNull(statement);
    return new Delegating(this) {
      @Override
      public Optional<Tuple> getTestCaseTuple() {
        return Stage.this.getTestCaseTuple();
      }

      @Override
      public Statement.Nested ongoingStatement() {
        return statement;
      }
    };
  }

  enum ExecutionLevel {
    SUITE,
    FIXTURE,
    ORACLE,
    ;
  }

  interface Factory {
    static <RESPONSE> Stage oracleLevelStageFor(Config config, TestItem testItem, RESPONSE response, Throwable throwable, Report report) {
      return new OracleLevelStage<>(response, ExecutionLevel.ORACLE, throwable, config, report, testItem);
    }

    static Stage frameworkStageFor(ExecutionLevel executionLevel, Config config, Tuple fixture) {
      return new FrameworkStage<>(fixture, executionLevel, config);
    }

    static Stage createWrappedStage(Stage input, Form<?>... args) {
      return new Delegating(input) {
        @Override
        public <U> U getArgument(int index) {
          check(index < sizeOfArguments(), () -> indexOutOfBounds(index, sizeOfArguments()));
          //noinspection unchecked
          return (U) args[index].apply(input);
        }

        @Override
        public int sizeOfArguments() {
          return args.length;
        }
      };
    }
  }
}
