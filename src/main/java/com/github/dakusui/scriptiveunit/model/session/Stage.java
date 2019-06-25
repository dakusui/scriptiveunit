package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.form.Form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.indexOutOfBounds;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;

/**
 * A stage is a part of session, where various activities defined as Funcs are
 * executed.
 */
public interface Stage {
  static Memo createMemo() {
    return new Memo.Impl();
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

  interface Memo extends Map<List<Object>, Object> {
    class Impl extends HashMap<List<Object>, Object> implements Memo {
      @Override
      public Object computeIfAbsent(List<Object> key,
                                    Function<? super List<Object>, ?> mappingFunction) {
        Object ret = mappingFunction.apply(key);
        put(key, ret);
        return ret;
      }
    }
  }
}
