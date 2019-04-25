package com.github.dakusui.scriptiveunit.drivers.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Form;
import com.github.dakusui.scriptiveunit.model.func.Func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.Actions.simple;
import static com.github.dakusui.scriptiveunit.core.Utils.prettify;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
public class Basic {
  private static final Logger LOGGER = LoggerFactory.getLogger(Basic.class);

  private static Consumer<String> out = System.err::println;//LOGGER::debug;

  public static void setOut(Consumer<String> out) {
    Basic.out = requireNonNull(out);
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> nop() {
    return input -> Actions.nop();
  }

  @SafeVarargs
  @SuppressWarnings("unused")
  @Scriptable
  public final Form<Action> sequential(Form<Action>... actions) {
    return (Stage input) -> Actions.sequential(
        stream(actions)
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.length]));
  }

  @SafeVarargs
  @SuppressWarnings("unused")
  @Scriptable
  public final Form<Action> concurrent(Form<Action>... actions) {
    return (Stage input) -> Actions.concurrent(
        stream(actions)
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.length]));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> fail(Form<String> in) {
    return input -> simple(() -> {
      throw new RuntimeException(in.apply(input));
    });
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> print(Form<?> in) {
    return input -> simple(prettify(
        "print",
        () -> {
          String s = in.apply(input).toString();
          System.out.println(s);
        })
    );
  }

  @Doc("Consumes a string composed from a given string 'msg' and given an input value " +
      "using the static field 'out' and returns the value.")
  @SuppressWarnings("unused")
  @Scriptable
  public <T> Form<T> debug(Form<String> msg, Form<T> in) {
    return input -> {
      T ret = in.apply(input);
      out.accept(msg.apply(input) + ":" + ret);
      return ret;
    };
  }

  @Doc("Prints to a given value to a 'dumb' output, which doesn't do anything.")
  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> dumb(@Doc("A value to be printed") Form<?> in) {
    return (Stage input) -> simple(prettify("dumb",
        // Even if it doesn't go anywhere, we must do 'apply'.
        (Runnable) () -> in.apply(input))
    );
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Form<Action> tag(Form<String> s) {
    return input -> simple(prettify(
        Objects.toString(s.apply(input)),
        () -> {
        }));
  }

  @SafeVarargs
  @SuppressWarnings("unused")
  @Scriptable
  public final Form<Boolean> perform(Form<Action>... actions) {
    return input -> {
      Utils.performActionWithLogging(
          Actions.sequential(Arrays
              .stream(actions)
              .map(actionFunc -> actionFunc.apply(input))
              .collect(toList()))
      );
      return true;
    };
  }

  int i = 0;
  @Scriptable
  public final Func.Memoized<Integer> increment() {
    return input -> i++;
  }
}
