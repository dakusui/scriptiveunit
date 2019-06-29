package com.github.dakusui.scriptiveunit.model.session.action;

import com.github.dakusui.actionunit.core.Action;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;

public class TestActionBuilder<I, O> {

  private final String name;

  private Source<I>  given;
  private Pipe<I, O> when;
  private Sink<O>    then;

  public TestActionBuilder(String name) {
    this.name = name;
  }

  public TestActionBuilder<I, O> given(Source<I> source) {
    this.given = source;
    return this;
  }

  public TestActionBuilder<I, O> when(Pipe<I, O> pipe) {
    this.when = pipe;
    return this;
  }

  public TestActionBuilder<I, O> then(Sink<O> sink) {
    this.then = sink;
    return this;
  }

  public Action build() {
    return simple(
        name,
        context -> then.accept(when.apply(given.apply(context), context), context)
    );
  }
}
