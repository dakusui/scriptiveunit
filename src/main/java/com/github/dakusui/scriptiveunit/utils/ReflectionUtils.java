package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.scriptiveunit.core.ObjectField;
import com.github.dakusui.scriptiveunit.exceptions.ResourceException;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.model.form.Form;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapIfNecessary;
import static com.github.dakusui.scriptiveunit.utils.CoreUtils.singletonCollector;
import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.util.stream.Collectors.toList;

public enum ReflectionUtils {
  ;

  public static Stream<String> allScriptsUnder(String prefix) {
    return allScriptsUnderMatching(prefix, Pattern.compile(".*"));
  }

  public static InputStream openResourceAsStream(String resourceName) {
    return ResourceException.scriptExists(getSystemResourceAsStream(resourceName), resourceName);
  }

  public static Stream<String> allScriptsUnderMatching(String prefix, Pattern pattern) {
    return new Reflections(prefix, new ResourcesScanner()).getResources(pattern).stream();
  }

  /**
   * Returns an annotation element of a specified type ({@code annotationClass})
   * attached to {@code annotatedElement}.
   * If it is not present, an empty {@code Optinal} will be returned.
   *
   * @param annotatedElement An element from which annotation object to be returned is retrieved.
   * @param annotationClass  An annotation class of the instance to be returned.
   */
  public static <T extends Annotation> Optional<T> getAnnotation(AnnotatedElement annotatedElement, Class<T> annotationClass) {
    return annotatedElement.isAnnotationPresent(annotationClass) ?
        Optional.of(annotatedElement.getAnnotation(annotationClass)) :
        Optional.empty();
  }

  public static List<Form> getAnnotatedMethods(Object object, Class<? extends Annotation> annotationClass,
      Map<String, String> aliases) {
    return Arrays.stream(object.getClass().getMethods())
        .filter(each -> each.isAnnotationPresent(annotationClass))
        .map(each -> Form.create(object, each, aliases)).collect(toList());
  }

  public static List<ObjectField> getAnnotatedFields(Object object, Class<? extends Annotation> annotationClass) {
    return Arrays
        .stream(object.getClass().getFields())
        .filter(each -> each.isAnnotationPresent(annotationClass))
        .map(each -> ObjectField.create(object, each))
        .collect(toList());
  }

  public static Stream<Class<?>> allTypesAnnotatedWith(String prefix, Class<? extends Annotation> annotation) {
    return findEntitiesUnder(prefix, (Reflections reflections) -> reflections.getTypesAnnotatedWith(annotation));
  }

  public static Stream<Class<?>> findEntitiesUnder(String prefix, Function<Reflections, Set<Class<?>>> func) {
    return func.apply(new Reflections(prefix, new TypeAnnotationsScanner(), new SubTypesScanner())).stream();
  }

  public static Object getFieldValue(Object object, Field field) {
    try {
      return field.get(object);
    } catch (IllegalAccessException e) {
      throw ScriptiveUnitException.wrapIfNecessary(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T createInstance(Class<? extends T> value, Object[] args) {
    try {
      return (T) chooseConstructor(value.getConstructors(), args).newInstance(args);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw wrapIfNecessary(e);
    }
  }

  public static Method chooseMethod(Method[] methods, Object[] args) {
    return (Method) chooseExecutable(methods, args);
  }

  static Constructor<?> chooseConstructor(Constructor<?>[] constructors, Object[] args) {
    return (Constructor<?>) chooseExecutable(constructors, args);
  }


  static Executable chooseExecutable(Executable[] executables, Object[] args) {
    Class<?>[] argTypes = Arrays.stream(args)
        .map(arg -> arg == null ? null : arg.getClass())
        .toArray(i -> (Class<?>[]) new Class[i]);
    return Arrays.stream(executables)
        .filter(each -> typesMatch(each.getParameterTypes(), argTypes))
        .collect(singletonCollector());
  }

  private static boolean typesMatch(Class<?>[] parameterTypes, Class<?>[] argTypes) {
    if (parameterTypes.length != argTypes.length)
      return false;
    for (int i = 0; i < parameterTypes.length; i++) {
      if (argTypes[i] == null)
        continue;
      if (!parameterTypes[i].isAssignableFrom(argTypes[i]))
        return false;
    }
    return true;
  }
}
