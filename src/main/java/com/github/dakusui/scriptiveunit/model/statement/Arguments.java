package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.model.func.Func;

import java.util.Iterator;

public interface Arguments extends Iterable<Statement> {
  static Arguments create(Statement.Factory statementFactory, Iterable<Func> args) {
    return () -> new Iterator<Statement>() {
      Iterator<Func> i = args.iterator();

      @Override
      public boolean hasNext() {
        return i.hasNext();
      }

      @Override
      public Statement next() {
        return statementFactory.create(i.next());
      }
    };
  }

  class Factory {
    private final Statement.Factory statementFactory;

    public Factory(Statement.Factory statementFactory) {
      this.statementFactory = statementFactory;
    }

    public Arguments create(Iterable<Func> args) {
      return Arguments.create(statementFactory, args);
    }
  }
}
