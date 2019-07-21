package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface CompatLoad {
  String     SCRIPT_NOT_SPECIFIED               = "";
  String     DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY = "scriptiveunit.target";
  CompatLoad DEFAULT_INSTANCE                   = new CompatLoad() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return CompatLoad.class;
    }

    @Override
    public String scriptSystemPropertyKey() {
      return DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY;
    }

    @Override
    public Class<? extends ScriptCompiler.Impl> with() {
      return ScriptCompiler.Impl.class;
    }
  };

  String scriptSystemPropertyKey() default DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY;

  Class<? extends ScriptCompiler.Impl> with() default ScriptCompiler.Impl.class;
}
