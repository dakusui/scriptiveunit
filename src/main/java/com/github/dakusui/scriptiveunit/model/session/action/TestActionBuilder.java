package com.github.dakusui.scriptiveunit.model.session.action;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;

public class TestActionBuilder<I, O> {

  public static  <I, O> TestActionBuilder<I, O> test() {
    return new TestActionBuilder<>();
  }

  private Source<I>  given;
  private Pipe<I, O> when;
  private Sink<O>    then;

  private TestActionBuilder() {
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
    return simple("", new ContextConsumer() {
      @Override
      public void accept(Context context) {
        then.accept(when.apply(given.apply(context), context), context);
      }
    });
  }
}
