package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.form.Form;

import java.util.Optional;
import java.util.function.BiFunction;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.indexOutOfBounds;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;

/**
 * A stage is a part of session, where various activities defined as Funcs are
 * executed.
 */
public interface Stage extends Form.Listener{
  static <U> U applyForm(Stage stage, Form<U> form, BiFunction<Form<U>, Stage, U> applier) {
    stage.enter(form);
    try {
      U ret = applier.apply(form, stage);
      stage.leave(form, ret);
      return ret;
    } catch (RuntimeException | Error e) {
      stage.fail(form, e);
      throw e;
    }
  }

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

  interface Default extends Stage {
    @Override
    default Optional<TestItem> getTestItem() {
      return Optional.empty();
    }

    @Override
    default void enter(Form form) {

    }

    @Override
    default void leave(Form form, Object value) {

    }

    @Override
    default void fail(Form form, Throwable t) {
    }
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

    static Stage createWrappedStage(Stage stage, Form<?>... args) {
      return new Delegating(stage) {
        @Override
        public <U> U getArgument(int index) {
          check(index < sizeOfArguments(), () -> indexOutOfBounds(index, sizeOfArguments()));
          //noinspection unchecked
          return (U) args[index].apply(stage);
        }

        @Override
        public int sizeOfArguments() {
          return args.length;
        }

        @Override
        public void enter(Form form) {
          stage.enter(form);
        }

        @Override
        public void leave(Form form, Object value) {
          stage.leave(form, value);
        }

        @Override
        public void fail(Form form, Throwable t) {
          stage.fail(form, t);
        }
      };
    }

    static Stage createFormListeningStage(Stage stage, Form.Listener formListener) {
      return new Stage() {
        @Override
        public ExecutionLevel getExecutionLevel() {
          return stage.getExecutionLevel();
        }

        @Override
        public Config getConfig() {
          return stage.getConfig();
        }

        @Override
        public int sizeOfArguments() {
          return stage.sizeOfArguments();
        }

        @Override
        public <T> T getArgument(int index) {
          return stage.getArgument(index);
        }

        @Override
        public Optional<Throwable> getThrowable() {
          return stage.getThrowable();
        }

        @Override
        public Optional<Tuple> getTestCaseTuple() {
          return stage.getTestCaseTuple();
        }

        @Override
        public <RESPONSE> Optional<RESPONSE> response() {
          return stage.response();
        }

        @Override
        public Optional<Report> getReport() {
          return stage.getReport();
        }

        @Override
        public Optional<TestItem> getTestItem() {
          return stage.getTestItem();
        }

        @Override
        public void enter(Form form) {
          formListener.enter(form);
        }

        @Override
        public void leave(Form form, Object value) {
          formListener.leave(form, value);
        }

        @Override
        public void fail(Form form, Throwable t) {
          formListener.fail(form, t);
        }
      };
    }
  }

}
