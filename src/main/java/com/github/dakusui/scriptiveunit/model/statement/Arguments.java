package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.model.func.Func;

import java.util.Iterator;
import java.util.LinkedList;

public interface Arguments extends Iterable<Statement> {
  static Arguments create(Statement.Factory statementFactory, Iterable<Func> args) {
    return new Arguments() {
      @Override
      public Iterator<Statement> iterator() {
        return new Iterator<Statement>() {
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

      @Override
      public String toString() {
        return format();
      }
    };
  }

  default String format() {
    return new LinkedList<String>() {{
      for (Statement each : Arguments.this) {
        add(each.format());
      }
    }}.toString();
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
