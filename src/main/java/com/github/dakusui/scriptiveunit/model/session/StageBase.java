package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.scriptiveunit.core.Config;

import java.util.Optional;

abstract class StageBase implements Stage.Default {
  private final Object response;
  private final Throwable throwable;
  private final Config config;
  private final Report report;

  StageBase(Object response, Throwable throwable, Config config, Report report) {
    this.response = response;
    this.throwable = throwable;
    this.config = config;
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
  public Config getConfig() {
    return config;
  }

  @Override
  public Optional<Report> getReport() {
    return Optional.ofNullable(report);
  }
}
