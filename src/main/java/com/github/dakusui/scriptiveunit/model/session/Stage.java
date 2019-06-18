package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.form.Form;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.utils.Checks.check;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.indexOutOfBounds;

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

  <T> T eval(String name, Function<List<Object>, T> def, Form... args);

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
