package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.scriptiveunit.core.JsonScript;

import java.util.Optional;

abstract class StageBase implements Stage.Default {
  private final Object     response;
  private final Throwable  throwable;
  private final JsonScript script;
  private final Report     report;

  StageBase(Object response, Throwable throwable, JsonScript script, Report report) {
    this.response = response;
    this.throwable = throwable;
    this.script = script;
    this.report = report;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <RESPONSE> Optional<RESPONSE> response() {
    return Optional.ofNullable((RESPONSE) response);
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
  public JsonScript getScript() {
    return script;
  }

  @Override
  public Optional<Report> getReport() {
    return Optional.ofNullable(report);
  }
}
