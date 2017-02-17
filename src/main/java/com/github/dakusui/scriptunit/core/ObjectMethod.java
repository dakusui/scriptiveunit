package com.github.dakusui.scriptunit.core;

import com.github.dakusui.scriptunit.annotations.AccessesTestParameter;
import com.github.dakusui.scriptunit.annotations.Doc;
import com.github.dakusui.scriptunit.annotations.Import;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static com.github.dakusui.scriptunit.exceptions.ScriptiveUnitException.wrap;
import static java.lang.String.format;
import static java.util.Arrays.stream;

/**
 * An interface that represents a pair of a method and object on which it should
 * be invoked.
 */
public interface ObjectMethod {
  String getName();

  int getParameterCount();

  Class<?>[] getParameterTypes();

  Doc getParameterDoc(int index);

  Doc doc();

  boolean isVarArgs();

  boolean isAccessor();

  Object invoke(Object... args);


  static ObjectMethod create(Object object, Method method, Map<String, String> aliases) {
    return new ObjectMethod() {
      @Override
      public String getName() {
        String baseName = method.getName();
        return
            aliases.containsKey(baseName) ?
                aliases.get(baseName) :
                aliases.containsKey(Import.Alias.ALL) ?
                    baseName :
                    null;
      }

      @Override
      public int getParameterCount() {
        return method.getParameterCount();
      }

      @Override
      public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
      }

      @Override
      public Doc getParameterDoc(int index) {
        Optional<? extends Annotation> docAnn = stream(method.getParameterAnnotations()[index]).filter(input -> input instanceof Doc).findFirst();
        return docAnn.isPresent() ?
            (Doc) docAnn.get() :
            Doc.NOT_AVAILABLE;
      }

      @Override
      public boolean isVarArgs() {
        return method.isVarArgs();
      }

      @Override
      public boolean isAccessor() {
        return method.isAnnotationPresent(AccessesTestParameter.class);
      }

      @Override
      public Object invoke(Object... args) {
        try {
          return method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
          String message = format("Failed to invoke %s#%s(%s) with %s", method.getDeclaringClass().getCanonicalName(), method.getName(), Arrays.toString(method.getParameterTypes()), Arrays.toString(args));
          throw wrap(e, message);
        }
      }

      @Override
      public Doc doc() {
        return method.isAnnotationPresent(Doc.class) ?
            method.getAnnotation(Doc.class) :
            Doc.NOT_AVAILABLE
            ;
      }

      @Override
      public String toString() {
        return String.format("%s(%s of %s)", method.getName(), method, object);
      }
    };
  }
}
