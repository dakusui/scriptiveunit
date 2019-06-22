package com.github.dakusui.scriptiveunit.testassets;

import com.github.dakusui.scriptiveunit.annotations.Import.Alias;
import com.github.dakusui.scriptiveunit.drivers.Arith;
import com.github.dakusui.scriptiveunit.drivers.Predicates;
import com.github.dakusui.scriptiveunit.drivers.actions.Basic;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.form.Form;
import org.junit.runner.RunWith;

@RunWith(ScriptiveUnit.class)
public class Driver1 {
  @SuppressWarnings("unused")
  @Import
  public final Object basic = new Basic();

  @SuppressWarnings("unused")
  @Import({
      @Alias(value = "*"),
      @Alias(value = "gt", as = ">"),
      @Alias(value = "ge", as = ">="),
      @Alias(value = "lt", as = "<"),
      @Alias(value = "le", as = "<="),
      @Alias(value = "eq", as = "=="),
      @Alias(value = "ifthen", as = "if_then")
  })

  public final Object arith = new Predicates();
  @SuppressWarnings("unused")
  @Import
  public final Object helloWorld = new HelloWorld();

  static class HelloWorld {
    @Doc({"Hello, world", "everyone"})
    @SuppressWarnings("unused")
    @Scriptable
    public Form<String> helloWorld() {
      return (Stage input) -> "Hello world";
    }

    @SuppressWarnings("unused")
    @Scriptable
    public Form<String> print(Form<String> s) {
      return (Stage input) -> {
        System.out.println(s.apply(input));
        return s.apply(input);
      };
    }
  }
}
