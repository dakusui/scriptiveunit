package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.core.ScriptLoader;
import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;

import java.util.Arrays;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapIfNecessary;

public enum Utils {
  ;

  static ScriptCompiler createScriptCompilerFrom(Compile compileAnnotation) {
    return ReflectionUtils.createInstance(compileAnnotation.with(), argValues(compileAnnotation.args()));
  }

  static ScriptLoader createScriptLoaderFrom(Load loadAnnotation) {
    return ReflectionUtils.createInstance(loadAnnotation.with(), argValues(loadAnnotation.args()));
  }

  @SuppressWarnings("unchecked")
  static Object[] argValues(Value[] values) {
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
