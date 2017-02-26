package com.github.dakusui.scriptiveunit.drivers.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.util.Objects;

import static com.github.dakusui.scriptiveunit.core.Utils.prettify;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@ReflectivelyReferenced
public class Basic {
  @ReflectivelyReferenced
  @Scriptable
  public Func<Action> nop() {
    return input -> Actions.nop();
  }

  @SafeVarargs
  @ReflectivelyReferenced
  @Scriptable
  public final Func<Action> sequential(Func<Action>... actions) {
    return (Stage input) -> Actions.sequential(
        stream(actions)
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.length]));
  }

  @SafeVarargs
  @ReflectivelyReferenced
  @Scriptable
  public final Func<Action> concurrent(Func<Action>... actions) {
    return (Stage input) -> Actions.concurrent(
        stream(actions)
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.length]));
  }

  @ReflectivelyReferenced
  @Scriptable
  public Func<Action> fail(Func<String> in) {
    return input -> Actions.simple(() -> {
      throw new RuntimeException(in.apply(input));
    });
  }

  @ReflectivelyReferenced
  @Scriptable
  public Func<Action> print(Func<?> in) {
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

  @Doc("Prints to a given value to a 'dumb' output.")
  @ReflectivelyReferenced
  @Scriptable
  public Func<Action> dumb(@Doc("A value to be printed") Func<?> in) {
    return input -> Actions.simple(prettify("dumb",
        // Even if it doesn't go anywhere, we must do 'apply'.
        () -> in.apply(input))
    );
  }

  @ReflectivelyReferenced
  @Scriptable
  public final Func<Action> tag(Func<String> s) {
    return input -> Actions.simple(new Runnable() {
      @Override
      public void run() {
      }

      @Override
      public String toString() {
        return Objects.toString(s.apply(input));
      }
    });
  }
}
