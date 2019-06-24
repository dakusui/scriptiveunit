package com.github.dakusui.scriptiveunit.model.desc.testitem;

import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface TestOracle {

  int getIndex();

  /**
   * Returns a string that describes this test oracle.
   * <p>
   * Note that this method always returns raw form (a string before being templated).
   */
  String getDescription();

  Definition definitionFor(TestItem testItem);

  interface Definition {
    Optional<Statement> before();

    Optional<Statement> given();

    Statement when();

    Statement then();

    Optional<Statement> onFailure();

    Optional<Statement> after();

    static Definition create(Statement.Factory statementFactory, List<Object> before, List<Object> given, List<Object> when, List<Object> then, List<Object> onFailure, List<Object> after) {
      requireNonNull(statementFactory);
      requireNonNull(when);
      requireNonNull(then);
      return new Definition() {
        @Override
        public Optional<Statement> before() {
          return Optional.ofNullable(before)
              .map(statementFactory::create);
        }

        @Override
        public Optional<Statement> given() {
          return Optional.ofNullable(given)
              .map(statementFactory::create);
        }

        @Override
        public Statement when() {
          return statementFactory.create(when);
        }

        @Override
        public Statement then() {
          return statementFactory.create(then);
        }

        @Override
        public Optional<Statement> onFailure() {
          return Optional.ofNullable(onFailure)
              .map(statementFactory::create);
        }

        @Override
        public Optional<Statement> after() {
          return Optional.ofNullable(after)
              .map(statementFactory::create);
        }
      };
    }
  }
}
