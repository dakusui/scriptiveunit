package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapIfNecessary;

public enum Utils {
  ;

  static ScriptCompiler createScriptCompilerFrom(Compile compileAnnotation) {
    return createInstance(compileAnnotation.with(), argValues(compileAnnotation.args()));
  }

  static Load.ScriptLoader createScriptLoaderFrom(Load loadAnnotation) {
    return createInstance(loadAnnotation.value(), argValues(loadAnnotation.args()));
  }

  @SuppressWarnings("unchecked")
  static <T> T createInstance(Class<? extends T> value, Object[] args) {
    try {
      return (T) chooseConstructor(
          value.getConstructors(),
          args).orElseThrow(RuntimeException::new)
          .newInstance(args);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw wrapIfNecessary(e);
    }
  }

  static Optional<Constructor<?>> chooseConstructor(Constructor<?>[] constructors, Object[] args) {
    Class<?>[] argTypes = Arrays.stream(args)
        .map(arg -> arg == null ? null : arg.getClass())
        .toArray(i -> (Class<?>[]) new Class[i]);
    return Arrays.stream(constructors)
        .filter(each -> typesMatch(each.getParameterTypes(), argTypes))
        .findFirst();
  }

  private static boolean typesMatch(Class<?>[] parameterTypes, Class<?>[] argTypes) {
    if (parameterTypes.length != argTypes.length)
      return false;
    for (int i = 0; i < parameterTypes.length; i++) {
      if (argTypes[i] == null)
        continue;
      ;
      if (!parameterTypes[i].isAssignableFrom(argTypes[i]))
        return false;
    }
    return true;
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
