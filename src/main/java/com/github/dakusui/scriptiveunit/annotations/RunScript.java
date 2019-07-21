package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.util.Properties;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface RunScript {
  String    SCRIPT_NOT_SPECIFIED               = "";
  String    DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY = "scriptiveunit.target";
  RunScript DEFAULT_INSTANCE                   = new RunScript() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return RunScript.class;
    }

    @Override
    public String scriptSystemPropertyKey() {
      return DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY;
    }

    @Override
    public Class<? extends ScriptCompiler.Default> compileWith() {
      return ScriptCompiler.Default.class;
    }
  };

  String scriptSystemPropertyKey() default DEFAULT_SCRIPT_SYSTEM_PROPERTY_KEY;

  Class<? extends ScriptCompiler.Default> compileWith() default ScriptCompiler.Default.class;

  enum Utils {
    ;

    public static String getScriptResourceNameFrom(Class<?> driverClass, Properties properties) {
      return getScriptResourceNameFrom(getLoadAnnotation(driverClass), properties);
    }

    static String getScriptResourceNameFrom(RunScript runScriptAnnotation, Properties properties) {
      return properties.getProperty(runScriptAnnotation.scriptSystemPropertyKey());
    }

    public static RunScript getLoadAnnotation(Class<?> driverClass) {
      return ReflectionUtils.getAnnotation(
          driverClass,
          RunScript.class,
          DEFAULT_INSTANCE);
    }

    public static Properties createPropertiesFor(
        RunScript load, String scriptResourceName) {
      Properties properties = new Properties();
      properties.put(load.scriptSystemPropertyKey(), scriptResourceName);
      return properties;
    }
  }
}
