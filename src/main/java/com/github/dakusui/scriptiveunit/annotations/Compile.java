package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface Compile {
  Class<? extends ScriptCompiler.Impl> with() default ScriptCompiler.Impl.class;

  Value[] args() default {};

}
