package com.github.dakusui.scriptunit.model.statement;

import com.github.dakusui.scriptunit.model.Func;

import java.util.Iterator;

public interface Arguments extends Iterable<Statement> {
  class Factory {
    private final Statement.Factory statementFactory;

    public Factory(Statement.Factory statementFactory) {
      this.statementFactory = statementFactory;
    }

    public Arguments create(Iterable<Object> args, Func.Invoker funcInvoker) {
      Iterator<Object> i = args.iterator();
      return () -> new Iterator<Statement>() {
        @Override
        public boolean hasNext() {
          return i.hasNext();
        }

        @Override
        public Statement next() {
          return statementFactory.create(i.next(), funcInvoker);
        }
      };
    }
  }
}
