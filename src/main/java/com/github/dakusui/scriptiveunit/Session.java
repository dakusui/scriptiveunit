package com.github.dakusui.scriptiveunit;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.Report;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestItem;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

public interface Session {
  Config getConfig();

  Report createReport(TestItem testItem);

  default Stage createConstraintConstraintGenerationStage(Statement.Factory statementFactory, Tuple tuple) {
    return Stage.Factory.createConstraintGenerationStage(this.getConfig(), statementFactory, tuple);
  }

  static Session create(Config config) {
    return new Impl(config);
  }

  class Impl implements Session {
    private final Config config;

    @SuppressWarnings("WeakerAccess")
    protected Impl(Config config) {
      this.config = config;
    }

    @Override
    public Config getConfig() {
      return this.config;
    }

    @Override
    public Report createReport(TestItem testItem) {
      return Report.create(getConfig(), testItem);
    }
  }
}
