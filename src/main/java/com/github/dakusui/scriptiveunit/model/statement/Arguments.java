package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.model.form.Form;

import java.util.Iterator;
import java.util.List;

public interface Arguments extends Iterable<Statement> {
  Form get(int i);

  int size();

  static Arguments create(Statement.Factory statementFactory, List<Form> args) {
    return new Arguments() {
      public Form get(int i) {
        return args.get(i);
      }

      @Override
      public int size() {
        return args.size();
      }

      @SuppressWarnings("NullableProblems")
      @Override
      public Iterator<Statement> iterator() {
        return new Iterator<Statement>() {
          Iterator<Form> i = args.iterator();

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
    };
  }

  class Factory {
    private final Statement.Factory statementFactory;

    public Factory(Statement.Factory statementFactory) {
      this.statementFactory = statementFactory;
    }

    public Arguments create(List<Form> args) {
      return Arguments.create(statementFactory, args);
    }
  }
}
