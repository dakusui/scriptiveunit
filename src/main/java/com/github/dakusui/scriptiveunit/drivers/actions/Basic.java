package com.github.dakusui.scriptiveunit.drivers.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.util.Arrays;
import java.util.Objects;

import static com.github.dakusui.actionunit.Actions.simple;
import static com.github.dakusui.scriptiveunit.core.Utils.prettify;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
public class Basic {
  @SuppressWarnings("unused")
  @Scriptable
  public Func<Action> nop() {
    return input -> Actions.nop();
  }

  @SafeVarargs
  @SuppressWarnings("unused")
  @Scriptable
  public final Func<Action> sequential(Func<Action>... actions) {
    return (Stage input) -> Actions.sequential(
        stream(actions)
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.length]));
  }

  @SafeVarargs
  @SuppressWarnings("unused")
  @Scriptable
  public final Func<Action> concurrent(Func<Action>... actions) {
    return (Stage input) -> Actions.concurrent(
        stream(actions)
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.length]));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Func<Action> fail(Func<String> in) {
    return input -> simple(() -> {
      throw new RuntimeException(in.apply(input));
    });
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Func<Action> print(Func<?> in) {
    return input -> simple(prettify(
        "print",
        () ->  {
          String s = in.apply(input).toString();
          System.out.println(s);
        })
    );
  }

  @Doc("Prints to a given value to a 'dumb' output, which doesn't do anything.")
  @SuppressWarnings("unused")
  @Scriptable
  public Func<Action> dumb(@Doc("A value to be printed") Func<?> in) {
    return (Stage input) -> simple(prettify("dumb",
        // Even if it doesn't go anywhere, we must do 'apply'.
        (Runnable) () -> in.apply(input))
    );
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Func<Action> tag(Func<String> s) {
    return input -> simple(prettify(
        Objects.toString(s.apply(input)),
        () -> {
        }));
  }

  @SafeVarargs
  @SuppressWarnings("unused")
  @Scriptable
  public final Func<Boolean> perform(Func<Action>... actions) {
    return input -> {
      Utils.performActionWithLogging(
          Actions.sequential(Arrays.stream(actions).map(
              actionFunc -> actionFunc.apply(input)
          ).collect(toList()))
      );
      return true;
    };
  }
}
