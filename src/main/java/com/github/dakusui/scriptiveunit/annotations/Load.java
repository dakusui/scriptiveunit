package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.loaders.TestSuiteLoader;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBasedTestSuiteLoader;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface Load {
  String SCRIPT_NOT_SPECIFIED               = "";
  String DEFAULT_SCRIPT_PACKAGE_PREFIX      = "";
  String DEFAULT_SCRIPT_NAME_PATTERN        = ".*\\.json";
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
    public String scriptPackagePrefix() {
      return DEFAULT_SCRIPT_PACKAGE_PREFIX;
    }

    @Override
    public String scriptNamePattern() {
      return DEFAULT_SCRIPT_NAME_PATTERN;
    }

    @Override
    public String scriptSystemPropertyKey() {
      return DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY;
    }

    @Override
    public Class<? extends TestSuiteLoader.Factory> with() {
      return JsonBasedTestSuiteLoader.Factory.class;
    }
  };

  String defaultScriptName() default SCRIPT_NOT_SPECIFIED;

  String scriptPackagePrefix() default DEFAULT_SCRIPT_PACKAGE_PREFIX;

  String scriptNamePattern() default DEFAULT_SCRIPT_NAME_PATTERN;

  String scriptSystemPropertyKey() default DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY;

  Class<? extends TestSuiteLoader.Factory> with() default JsonBasedTestSuiteLoader.Factory.class;
}
