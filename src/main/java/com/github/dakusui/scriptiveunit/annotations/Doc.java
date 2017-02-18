package com.github.dakusui.scriptiveunit.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ TYPE, METHOD, PARAMETER, FIELD })
public @interface Doc {
  Doc NOT_AVAILABLE = new Doc() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Doc.class;
    }

    @Override
    public String[] value() {
      return new String[]{"(n/a)"};
    }
  };

  String[] value() default {};
}
