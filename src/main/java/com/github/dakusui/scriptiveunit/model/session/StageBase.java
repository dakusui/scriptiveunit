package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.form.FormInvoker;

import java.util.Optional;

abstract class StageBase<RESPONSE> implements Stage {
  private final RESPONSE response;
  private final ExecutionLevel executionLevel;
  private final Throwable throwable;
  private final Config config;
  private final Report report;
  private final FormInvoker.Memo memo;

  StageBase(RESPONSE response, ExecutionLevel executionLevel, Throwable throwable, Config config, Report report) {
    this.response = response;
    this.executionLevel = executionLevel;
    this.throwable = throwable;
    this.config = config;
    this.report = report;
    this.memo = FormInvoker.createMemo();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<RESPONSE> response() {
    return Optional.ofNullable(response);
  }

  @Override
  public ExecutionLevel getExecutionLevel() {
    return executionLevel;
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
}
