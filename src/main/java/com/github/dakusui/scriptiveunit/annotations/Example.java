package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;

@Compile(
    with = Example.Compiler.class,
    args = @Value("script.name"))
public class Example {
  public static class Compiler extends ScriptCompiler.Compat {
    public Compiler(JsonScript script) {
      super(script);
    }
  }
}
