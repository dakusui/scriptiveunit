package com.github.dakusui.scriptiveunit.model.statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StatementRegistry {
  Optional<Statement> lookUp(String formName);

  static StatementRegistry create(Statement.Factory statementFactory, Map<String, List<Object>> clauses) {
    return new StatementRegistry() {
      Map<String, Statement> statements = new HashMap<>();

      @Override
      public Optional<Statement> lookUp(String formName) {
        return clauses.containsKey(formName) ?
            Optional.of(statements.computeIfAbsent(
                formName,
                name -> statementFactory.create(clauses.get(name)))) :
            Optional.empty();
      }
    };
  }
}
