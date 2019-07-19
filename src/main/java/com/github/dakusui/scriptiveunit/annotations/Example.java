package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;

@Compile(
    value = Load.ScriptLoader.class,
    with = Example.Compiler.class)
public class Example {
  public static class Compiler extends ScriptCompiler.Impl {
public Compiler(JsonScript script) {
      super(script);
    }
  }
}
