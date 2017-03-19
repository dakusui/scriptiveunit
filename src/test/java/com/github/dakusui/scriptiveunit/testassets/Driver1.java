package com.github.dakusui.scriptiveunit.testassets;

import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;
import org.junit.runner.RunWith;

@RunWith(ScriptiveUnit.class)
public class Driver1 {
  @SuppressWarnings("unused")
  @Import
  public final Object helloWorld = new HelloWorld();

  static class HelloWorld {
    @Doc({"Hello, world", "everyone"})
    @SuppressWarnings("unused")
    @Scriptable
    public Func<String> helloWorld() {
      return (Stage input) -> "Hello world";
    }

    @SuppressWarnings("unused")
    @Scriptable
    public Func<String> print(Func<String> s) {
      return (Stage input) -> {
        System.out.println(s.apply(input));
        return s.apply(input);
      };
    }
  }
}
