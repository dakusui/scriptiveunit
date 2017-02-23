package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

public interface Stage {
  Statement.Factory getStatementFactory();

  Tuple getTestCaseTuple();

  <RESPONSE> RESPONSE response();

  Type getType();

  <T> T getArgument(int index);

  int sizeOfArguments();

  Config getConfig();

  enum Type {
    TOPLEVEL,
    SETUP_BEFORE_ALL,
    SETUP,
    BEFORE,
    GIVEN,
    WHEN,
    THEN,
    AFTER,
    TEARDOWN,
    TEARDOWN_AFETR_ALL;

    public Stage create(TestSuiteDescriptor testSuiteDescriptor, Tuple fixture, Object response) {
      Statement.Factory statementFactory = new Statement.Factory(testSuiteDescriptor);
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
        public <RESPONSE> RESPONSE response() {
          if (response == null)
            throw new UnsupportedOperationException();
          //noinspection unchecked
          return (RESPONSE) response;
        }

        @Override
        public Type getType() {
          return Type.this;
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
        public Config getConfig() {
          return testSuiteDescriptor.getConfig();
        }
      };
    }
  }
}
