package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.core.Script;

import java.lang.annotation.Retention;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapIfNecessary;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface Load {
  /**
   * A constructor of the returned class will be invoked by the framework.
   *
   * @return A class with which script loading happens.
   */
  Class<? extends ScriptLoader> value();

  Value[] args() default {};

  interface ScriptLoader {
    Script<?, ?, ?, ?> load();
  }

  abstract class BaseScriptLoader implements ScriptLoader {
  }

  class DefaultScriptLoader extends BaseScriptLoader {
    @Override
    public Script<?, ?, ?, ?> load() {
      return null;
    }
  }
}
