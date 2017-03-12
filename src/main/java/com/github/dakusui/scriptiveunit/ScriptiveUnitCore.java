package com.github.dakusui.scriptiveunit;

import com.github.dakusui.scriptiveunit.doc.Documentation;
import org.junit.runner.Result;

import java.util.List;

/**
 * A facade to ScriptiveUnit's functionalities.
 */
public class ScriptiveUnitCore {
  public ScriptiveUnitCore() {
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
    return null;
  }

  public List<Class<?>> listDrivers(String packagePrefix) {
    return null;
  }

  public List<String> listRunners() {
    return null;
  }

  public List<Class<?>> listSuiteSets(String packagePrefix) {
    return null;
  }

  public List<String> listScripts() {
    return null;
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
