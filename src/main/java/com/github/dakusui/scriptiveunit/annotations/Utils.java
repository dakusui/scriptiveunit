package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.core.ScriptLoader;
import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;

import java.util.Arrays;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapIfNecessary;

public enum Utils {
  ;

  public static ScriptCompiler createScriptCompilerFrom(CompileWith compileWithAnnotation) {
    return ReflectionUtils.createInstance(compileWithAnnotation.with(), argValues(compileWithAnnotation.args()));
  }

  public static ScriptLoader createScriptLoaderFrom(LoadBy loadByAnnotation) {
    return ReflectionUtils.createInstance(loadByAnnotation.value(), argValues(loadByAnnotation.args()));
  }

  @SuppressWarnings("unchecked")
  public static Object[] argValues(Value[] values) {
    return Arrays.stream(values)
        .map(each -> {
          try {
            return each.type().newInstance().apply(each.value());
          } catch (InstantiationException | IllegalAccessException e) {
            throw wrapIfNecessary(e);
          }
        })
        .toArray(Object[]::new);
  }

}
