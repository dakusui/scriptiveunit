package com.github.dakusui.scriptiveunit.testassets;

import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Import.Alias;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.libs.Arith;
import com.github.dakusui.scriptiveunit.libs.Core;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.actions.Basic;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.junit.runner.RunWith;

import java.util.List;

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
  public final Object predicates = new Predicates();

  @SuppressWarnings("unused")
  @Import
  public final Object arith = new Arith();

  @SuppressWarnings("unused")
  @Import
  public final Object helloWorld = new HelloWorld();

  @SuppressWarnings("unused")
  @Import
  public final Object core = new Core();

  static class HelloWorld {
    @Doc({"Hello, world", "everyone"})
    @SuppressWarnings("unused")
    @Scriptable
    public Value<String> helloWorld() {
      return (Stage input) -> "Hello world";
    }

    @SuppressWarnings("unused")
    @Scriptable
    public Value<String> printString(Value<String> s) {
      return (Stage input) -> {
        System.out.println(s.apply(input));
        return s.apply(input);
      };
    }

    @SuppressWarnings("unused")
    @Scriptable
    public Value<String> printVarargs(List<Value<String>> strings) {
      StringBuffer buf = new StringBuffer();
      return (Stage input) -> {
        strings.forEach(s -> {
          System.out.println(s.apply(input));
          buf.append(s.apply(input));
        });
        return buf.toString();
      };
    }
  }
}