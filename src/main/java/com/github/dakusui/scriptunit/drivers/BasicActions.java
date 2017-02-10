package com.github.dakusui.scriptunit.drivers;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.annotations.Scriptable;
import com.github.dakusui.scriptunit.model.Func;
import com.github.dakusui.scriptunit.model.Stage;

@ReflectivelyReferenced
public class BasicActions {
  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage> Func<T, Action> nop() {
    return input -> Actions.nop();
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage> Func<T, Action> print(Func<T, ?> in) {
    return input -> Actions.simple(
        new Runnable() {
          @Override
          public void run() {
            System.out.println(in);
          }

          @Override
          public String toString() {
            return "print";
          }
        }
    );
  }
}
