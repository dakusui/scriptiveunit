package com.github.dakusui.scriptunit.testutils.drivers.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.annotations.Scriptable;
import com.github.dakusui.scriptunit.model.func.Func;
import com.github.dakusui.scriptunit.model.Stage;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@ReflectivelyReferenced
public class Basic {
  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage> Func<T, Action> nop() {
    return input -> Actions.nop();
  }

  @SafeVarargs
  @ReflectivelyReferenced
  @Scriptable
  public final <T extends Stage> Func<T, Action> sequential(Func<T, Action>... actions) {
    return (T input) -> Actions.sequential(
        stream(actions)
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.length]));
  }

  @SafeVarargs
  @ReflectivelyReferenced
  @Scriptable
  public final <T extends Stage> Func<T, Action> concurrent(Func<T, Action>... actions) {
    return (T input) -> Actions.concurrent(
        stream(actions)
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.length]));
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage> Func<T, Action> fail(Func<T, String> in) {
    return input -> Actions.simple(() -> {
      throw new RuntimeException(in.apply(input));
    });
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage> Func<T, Action> print(Func<T, ?> in) {
    return input -> Actions.simple(
        new Runnable() {
          @Override
          public void run() {
            System.out.println(in.apply(input));
          }

          @Override
          public String toString() {
            return "print";
          }
        }
    );
  }
}
