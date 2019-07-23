package com.github.dakusui.scriptiveunit.model.stage;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.session.Report;

import java.util.Optional;
import java.util.function.BiFunction;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.indexOutOfBounds;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;

/**
 * A stage is a part of session, where various activities defined as Funcs are
 * executed.
 */
public interface Stage extends Value.Listener {
  static <U> U evaluateValue(Stage stage, Value<U> value, BiFunction<Value<U>, Stage, U> applier) {
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

  static Stage createFixtureLevelStage(Tuple fixtureTuple, Script script) {
    return Factory.frameworkStageFor(script, fixtureTuple);
  }

  static Stage createSuiteLevelStage(Tuple suiteLevelTuple, Script script) {
    return createFixtureLevelStage(suiteLevelTuple, script);
  }

  static Stage createOracleLevelStage(Report report, Script script, IndexedTestCase testCase, TestOracle testOracle) {
    return Factory.oracleStageFor(
        script,
        null,
        testCase, testOracle, report, null
    );
  }

  Script getScript();

  int sizeOfArguments();

  <T> T getArgument(int index);

  Optional<Throwable> getThrowable();

  Optional<Tuple> getTestCaseTuple();

  <RESPONSE> Optional<RESPONSE> response();

  Optional<Report> getReport();

  Optional<IndexedTestCase> getTestCase();

  Optional<TestOracle> getTestOracle();

  interface Default extends Stage {
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


  abstract class Base implements Default {
    private final Object    response;
    private final Throwable throwable;
    private final Script    script;
    private final Report    report;

    Base(Object response, Throwable throwable, Script script, Report report) {
      this.response = response;
      this.throwable = throwable;
      this.script = script;
      this.report = report;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RESPONSE> Optional<RESPONSE> response() {
      return Optional.ofNullable((RESPONSE) response);
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
    public Optional<Throwable> getThrowable() {
      return Optional.ofNullable(throwable);
    }

    @Override
    public Script getScript() {
      return script;
    }

    @Override
    public Optional<Report> getReport() {
      return Optional.ofNullable(report);
    }
  }

  interface Factory {
    static <RESPONSE> Stage oracleStageFor(Script script, RESPONSE response, IndexedTestCase testCase, TestOracle testOracle, Report report, Throwable throwable) {
      return new OracleLevelStage(response, throwable, script, report, testCase, testOracle);
    }

    static Stage frameworkStageFor(Script script, Tuple fixture) {
      return new FrameworkStage(fixture, script);
    }

    static Stage createWrappedStage(Stage stage, Value<?>... args) {
      return new Delegating(stage) {
        @SuppressWarnings("unchecked")
        @Override
        public <U> U getArgument(int index) {
          check(index < sizeOfArguments(), () -> indexOutOfBounds(index, sizeOfArguments()));
          return (U) args[index].apply(stage);
        }

        @Override
        public Optional<IndexedTestCase> getTestCase() {
          return stage.getTestCase();
        }

        @Override
        public Optional<TestOracle> getTestOracle() {
          return stage.getTestOracle();
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

    static Stage createValueListeningStage(Stage stage, Value.Listener formListener) {
      return new Stage() {
        @Override
        public Script getScript() {
          return stage.getScript();
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
        public Optional<IndexedTestCase> getTestCase() {
          return stage.getTestCase();
        }

        @Override
        public Optional<TestOracle> getTestOracle() {
          return stage.getTestOracle();
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
