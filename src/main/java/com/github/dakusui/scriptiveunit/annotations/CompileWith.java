package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface CompileWith {
  Class<? extends ScriptCompiler> value() default ScriptCompiler.Default.class;

  Value[] args() default {};

}
