package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;

import java.util.Optional;

public interface StageFactory {
  static <RESPONSE> Stage _create(Stage.Type type, Config config, Tuple testCase, TestItem testItem, RESPONSE response, Throwable throwable, Report report) {
    return new Stage() {
      @Override
      public Optional<Tuple> getTestCaseTuple() {
        return Optional.ofNullable(testCase);
      }

      @SuppressWarnings("unchecked")
      @Override
      public Optional<RESPONSE> response() {
        return Optional.ofNullable(response);
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
      public Optional<Throwable> getThrowable() {
        return Optional.ofNullable(throwable);
      }

      @Override
      public Config getConfig() {
        return config;
      }

      @Override
      public Optional<Report> getReport() {
        return Optional.ofNullable(report);
      }

      @Override
      public Optional<TestItem> getTestItem() {
        return Optional.ofNullable(testItem);
      }
    };
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
  static Stage createFixtureLevelStage(Stage.Type type, Tuple fixture, Config config) {
    return _create(
        type,
        config,
        fixture,
        null,
        null,
        null,
        null);
  }
}
