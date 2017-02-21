package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

public interface Stage {
  Statement.Factory getStatementFactory();

  Tuple getTestCaseTuple();

  <RESPONSE> RESPONSE response();

  Type getType();

  <T> T getArgument(int index);

  int sizeOfArguments();

  enum Type {
    SETUP_BEFORE_ALL,
    SETUP,
    GIVEN,
    WHEN,
    THEN;

    public Stage create(TestSuiteDescriptor testSuiteDescriptor, Tuple fixture, Object response) {
      return _create(new Statement.Factory(testSuiteDescriptor), fixture, response);
    }

    private Stage _create(Statement.Factory statementFactory, Tuple fixture, Object response) {
      Type type = this;
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
      };
    }
  }
}
