package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.loaders.json.JsonBasedTestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface Load {
  String SCRIPT_NOT_SPECIFIED               = "";
  String DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY = "scriptiveunit.target";
  Load   DEFAULT_INSTANCE                   = new Load() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Load.class;
    }

    @Override
    public String defaultScriptName() {
      return "";
    }

    @Override
    public String scriptSystemPropertyKey() {
      return DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY;
    }

    @Override
    public Class<? extends TestSuiteDescriptorLoader> with() {
      return JsonBasedTestSuiteDescriptorLoader.class;
    }
  };

  String defaultScriptName() default SCRIPT_NOT_SPECIFIED;

  String scriptSystemPropertyKey() default DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY;

  Class<? extends TestSuiteDescriptorLoader> with() default JsonBasedTestSuiteDescriptorLoader.class;
}
