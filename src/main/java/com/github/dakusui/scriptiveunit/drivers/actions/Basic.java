package com.github.dakusui.scriptiveunit.drivers.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormList;
import com.github.dakusui.scriptiveunit.model.form.Func;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.utils.ActionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.Actions.simple;
import static com.github.dakusui.scriptiveunit.model.form.Func.*;
import static com.github.dakusui.scriptiveunit.utils.StringUtils.prettify;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
public class Basic {
  private static final Logger LOGGER = LoggerFactory.getLogger(Basic.class);

  private static Consumer<String> out = System.err::println;//LOGGER::debug;
  private Map<Func.Call, Object> memo = new HashMap<>();

  public static void setOut(Consumer<String> out) {
    Basic.out = requireNonNull(out);
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<Action> nop() {
    return input -> Actions.nop();
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Form<Action> sequential(FormList<Action> actions) {
    return (Stage input) -> Actions.sequential(
        actions.stream()
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.size()]));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Form<Action> concurrent(FormList<Action> actions) {
    return (Stage input) -> Actions.concurrent(
        actions.stream()
            .map(each -> each.apply(input))
            .collect(toList())
            .toArray(new Action[actions.size()]));
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
  public final Form<Action> tag(Form<String> string) {
    return input -> simple(prettify(
        Objects.toString(string.apply(input)),
        () -> {
        }));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Form<Boolean> perform(FormList<Action> actions) {
    return input -> {
      ActionUtils.performActionWithLogging(
          Actions.sequential(
              actions.stream()
              .map(actionFunc -> actionFunc.apply(input))
              .collect(toList()))
      );
      return true;
    };
  }

  private int i = 0;

  @Scriptable
  public final Func<Integer> increment() {
    return memoize(
        createFunc(funcId(), input -> i++)
    );
  }

  @Scriptable
  public final Func<Integer> op(Form<Integer> a, Form<Integer> b) {
    return Func.memoize(
        new Func.Builder<Integer>(Func.funcId())
            .func(objects -> (Integer) objects[0] + (Integer) objects[1] + i++)
            .addParameter(a)
            .addParameter(b)
            .build()
    );
  }
}
