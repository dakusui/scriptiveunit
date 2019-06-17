package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.form.Form;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

abstract class Delegating implements Stage {
  private final Stage target;

  Delegating(Stage stage) {
    this.target = requireNonNull(stage);
  }

  @Override
  public Optional<Tuple> getTestCaseTuple() {
    return this.target.getTestCaseTuple();
  }

  @Override
  public <RESPONSE> Optional<RESPONSE> response() {
    return this.target.response();
  }

  @Override
  public ExecutionLevel getExecutionLevel() {
    return this.target.getExecutionLevel();
  }

  @Override
  public <T> T getArgument(int index) {
    return this.target.getArgument(index);
  }

  @Override
  public int sizeOfArguments() {
    return this.target.sizeOfArguments();
  }

  @Override
  public Optional<Throwable> getThrowable() {
    return this.target.getThrowable();
  }

  @Override
  public Config getConfig() {
    return this.target.getConfig();
  }

  @Override
  public Optional<Report> getReport() {
    return this.target.getReport();
  }

  @Override
  public Optional<TestItem> getTestItem() {
    return this.target.getTestItem();
  }

  @Override
  public <T> T eval(String name, Function<List<Object>, T> def, Form... args) {
    return target.eval(name, def, args);
  }
}
