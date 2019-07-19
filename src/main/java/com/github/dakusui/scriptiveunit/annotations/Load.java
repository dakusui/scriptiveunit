package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.core.ScriptLoader;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface Load {
  /**
   * A constructor of the returned class will be invoked by the framework.
   *
   * @return A class with which script loading happens.
   */
  Class<? extends ScriptLoader> with();

  Value[] args() default {};

}
