package com.github.dakusui.scriptiveunit;

import com.github.dakusui.scriptiveunit.ScriptiveSuiteSet.SuiteScripts;
import com.github.dakusui.scriptiveunit.ScriptiveSuiteSet.SuiteScripts.Streamer;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.core.Description;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.github.dakusui.scriptiveunit.exceptions.FacadeException.validateDriverClass;
import static com.github.dakusui.scriptiveunit.exceptions.FacadeException.validateSuiteSetClass;
import static java.util.stream.Collectors.toList;

/**
 * A facade to {@code ScriptiveUnit}'s functionalities.
 */
public class ScriptiveCore {
  public ScriptiveCore() {
  }

  public Description describeFunction(Class<?> driverClass, String scriptResourceName, String functionName) {
    try {
      return new ScriptiveUnit(
          validateDriverClass(driverClass),
          new Config.Builder(driverClass, new Properties())
              .withScriptResourceName(scriptResourceName)
              .build()
      ).describeFunction(driverClass.newInstance(), functionName);
    } catch (Throwable throwable) {
      throw ScriptiveUnitException.wrap(throwable);
    }
  }

  public List<String> listFunctions(Class<?> driverClass, String scriptResourceName) {
    try {
      return new ScriptiveUnit(
          validateDriverClass(driverClass),
          new Config.Builder(driverClass, new Properties())
              .withScriptResourceName(scriptResourceName)
              .build()
      ).getFormNames(driverClass.newInstance());
    } catch (Throwable throwable) {
      throw ScriptiveUnitException.wrap(throwable);
    }
  }

  public List<Class<?>> listDrivers(String packagePrefix) {
    return Utils.allTypesAnnotatedWith(packagePrefix, RunWith.class)
        .filter(aClass -> aClass.getAnnotation(RunWith.class).value().equals(ScriptiveUnit.class))
        .collect(toList());
  }

  public List<String> listRunners() {
    return Arrays.stream(ScriptiveUnit.Mode.values()).map((ScriptiveUnit.Mode mode) -> Utils.toCamelCase(mode.name())).collect(toList());
  }

  public List<Class<?>> listSuiteSets(String packagePrefix) {
    return Utils.allTypesAnnotatedWith(packagePrefix, RunWith.class)
        .filter(aClass -> aClass.getAnnotation(RunWith.class).value().equals(ScriptiveSuiteSet.class))
        .collect(toList());
  }

  public List<String> listScripts(Class<?> suiteSetClass) {
    return new Streamer(validateSuiteSetClass(suiteSetClass).getAnnotation(SuiteScripts.class)).stream().collect(toList());
  }

  public Result runSuiteSet(Class<?> suiteSetClass) {
    return JUnitCore.runClasses(suiteSetClass);
  }

  public Result runScript(Class<?> driverClass, String scriptResourceName) {
    try {
      return new JUnitCore().run(new ScriptiveUnit(driverClass, new Config.Builder(driverClass, new Properties()).withScriptResourceName(scriptResourceName).build()));
    } catch (Throwable throwable) {
      throw ScriptiveUnitException.wrap(throwable);
    }
  }
}
