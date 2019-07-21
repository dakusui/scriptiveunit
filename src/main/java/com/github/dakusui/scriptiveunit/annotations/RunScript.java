package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.core.ScriptLoader;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface RunScript {
  LoadBy loader() default @LoadBy(value = ScriptLoader.FromResourceSpecifiedBySystemProperty.class);

  CompileWith compiler() default @CompileWith();
}
