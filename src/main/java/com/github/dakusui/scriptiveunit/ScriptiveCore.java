package com.github.dakusui.scriptiveunit;

import com.github.dakusui.scriptiveunit.ScriptiveSuiteSet.SuiteScripts;
import com.github.dakusui.scriptiveunit.ScriptiveSuiteSet.SuiteScripts.Streamer;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.doc.Documentation;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.exceptions.FacadeException.validateDriverClass;
import static com.github.dakusui.scriptiveunit.exceptions.FacadeException.validateSuiteSetClass;
import static java.util.stream.Collectors.toList;

/**
 * A facade to ScriptiveUnit's functionalities.
 */
public class ScriptiveCore {
  public ScriptiveCore() {
  }

  public Documentation describeFunction(Class<?> driverClass, String scriptResourceName, String functionName) {
    return null;
  }

  public Documentation describeDriver(Class<?> driverClass) {
    return null;
  }

  public Documentation describeRunner(String runnerName) {
    return null;
  }

  public Documentation describeSuiteSet(Class<?> suiteSetClass) {
    return null;
  }

  public Documentation describeScript(String scriptResourceName) {
    return null;
  }

  public List<String> listFunctions(Class<?> driverClass, String scriptResourceName) {
    try {
      new ScriptiveUnit(
          validateDriverClass(driverClass),
          new Config.Builder(driverClass, new Properties())
              .withScriptResourceName(scriptResourceName)
              .build()
      );
    } catch (Throwable throwable) {
      throw ScriptiveUnitException.wrap(throwable);
    }
    // getObjectMethodsFromImportedFieldsInObject
    return null;
  }

  public List<Class<?>> listDrivers(String packagePrefix) {
    return Utils.allTypesAnnotatedWith(packagePrefix, RunWith.class)
        .filter(aClass -> aClass.getAnnotation(RunWith.class).value().equals(ScriptiveUnit.class))
        .collect(toList());
  }

  public List<String> listRunners() {
    return Arrays.stream(GroupedTestItemRunner.Type.values()).map(new Function<GroupedTestItemRunner.Type, String>() {
      @Override
      public String apply(GroupedTestItemRunner.Type type) {
        return Utils.toCamelCase(type.name());
      }
    }).collect(toList());
  }

  public List<Class<?>> listSuiteSets(String packagePrefix) {
    return Utils.allTypesAnnotatedWith(packagePrefix, RunWith.class)
        .filter(aClass -> aClass.getAnnotation(RunWith.class).value().equals(ScriptiveSuiteSet.class))
        .collect(toList());
  }

  public List<String> listScripts(Class<?> suiteSetClass) {
    return new Streamer(validateSuiteSetClass(suiteSetClass).getAnnotation(SuiteScripts.class)).stream().collect(toList());
  }

  public Result runSuiteSet() {
    return null;
  }

  public Result runScript(Class<?> driverClass, String scriptResourceName) {
    return null;
  }

  public <T> T runFunction(Class<?> driverClass, String scriptResourceName, String functionName) {
    return null;
  }
}
