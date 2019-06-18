package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.scriptiveunit.core.ObjectField;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.exceptions.ResourceException;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
   * If it is not present, {@code defaultInstance} will be returned.
   *
   * @param annotatedElement An element from which annotation object to be returned is retrieved.
   * @param annotationClass  An annotation class of the instance to be returned.
   * @param defaultInstance  An annotation object to be returned in the {@code annotatedElement} doesn't have it.
   */
  public static <T extends Annotation> T getAnnotation(AnnotatedElement annotatedElement, Class<T> annotationClass,
                                                       T defaultInstance) {
    return annotatedElement.isAnnotationPresent(annotationClass) ?
        annotatedElement.getAnnotation(annotationClass) :
        defaultInstance;
  }

  public static List<ObjectMethod> getAnnotatedMethods(Object object, Class<? extends Annotation> annotationClass,
                                                       Map<String, String> aliases) {
    return Arrays.stream(object.getClass().getMethods()).filter(each -> each.isAnnotationPresent(annotationClass))
        .map(each -> ObjectMethod.create(object, each, aliases)).collect(toList());
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

  public static <T extends Annotation> T getAnnotationWithDefault(Class javaClass, T defaultValue) {
    @SuppressWarnings("unchecked") T ret = (T) javaClass.<T>getAnnotation(defaultValue.annotationType());
    return ret != null ?
        ret :
        defaultValue;
  }

  public static Object getFieldValue(Object object, Field field) {
    try {
      return field.get(object);
    } catch (IllegalAccessException e) {
      throw ScriptiveUnitException.wrap(e);
    }
  }
}
