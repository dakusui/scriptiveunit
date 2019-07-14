package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.form.Value;

import java.util.Optional;
import java.util.function.BiFunction;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.indexOutOfBounds;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;

/**
 * A stage is a part of session, where various activities defined as Funcs are
 * executed.
 */
public interface Stage extends Value.Listener{
  static <U> U applyForm(Stage stage, Value<U> value, BiFunction<Value<U>, Stage, U> applier) {
    stage.enter(value);
    try {
      U ret = applier.apply(value, stage);
      stage.leave(value, ret);
      return ret;
    } catch (RuntimeException | Error e) {
      stage.fail(value, e);
      throw e;
    }
  }

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
    default void enter(Value value) {

    }

    @Override
    default void leave(Value form, Object value) {

    }

    @Override
    default void fail(Value value, Throwable t) {
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
      return new OracleLevelStage<>(response, throwable, config, report, testItem);
    }

    static Stage frameworkStageFor( Config config, Tuple fixture) {
      return new FrameworkStage<>(fixture, config);
    }

    static Stage createWrappedStage(Stage stage, Value<?>... args) {
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
        public void enter(Value value) {
          stage.enter(value);
        }

        @Override
        public void leave(Value form, Object value) {
          stage.leave(form, value);
        }

        @Override
        public void fail(Value value, Throwable t) {
          stage.fail(value, t);
        }
      };
    }

    static Stage createFormListeningStage(Stage stage, Value.Listener formListener) {
      return new Stage() {
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
        public void enter(Value value) {
          formListener.enter(value);
        }

        @Override
        public void leave(Value form, Object value) {
          formListener.leave(form, value);
        }

        @Override
        public void fail(Value value, Throwable t) {
          formListener.fail(value, t);
        }
      };
    }
  }

}
