package com.github.dakusui.scriptiveunit;

import com.github.dakusui.scriptiveunit.core.Description;
import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.runners.RunningMode;
import com.github.dakusui.scriptiveunit.runners.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.runners.ScriptiveSuiteSet.SuiteScripts;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.utils.DriverUtils;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import com.github.dakusui.scriptiveunit.utils.StringUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static com.github.dakusui.scriptiveunit.exceptions.FacadeException.validateDriverClass;
import static com.github.dakusui.scriptiveunit.exceptions.FacadeException.validateSuiteSetClass;
import static com.github.dakusui.scriptiveunit.exceptions.ResourceException.functionNotFound;
import static java.util.stream.Collectors.toList;

/**
 * A facade to {@code ScriptiveUnit}'s functionalities.
 */
public class ScriptiveCore {
  public ScriptiveCore() {
  }

  public Description describeFunction(Class<?> driverClass, String scriptResourceName, String functionName) {
    try {
      final ScriptiveUnit scriptiveUnit = new ScriptiveUnit(
          validateDriverClass(driverClass),
          new JsonScript.Standard(driverClass, new Properties(), scriptResourceName)
      );
      return Utils.describeForm(scriptiveUnit, driverClass.newInstance(), functionName);
    } catch (Throwable throwable) {
      throw ScriptiveUnitException.wrapIfNecessary(throwable);
    }
  }

  public List<String> listFunctions(Class<?> driverClass, String scriptResourceName) {
    try {
      return Utils.getFormNames(new ScriptiveUnit(
          validateDriverClass(driverClass),
          new JsonScript.Standard(driverClass, new Properties(), scriptResourceName)
      ), driverClass.newInstance());
    } catch (Throwable throwable) {
      throw ScriptiveUnitException.wrapIfNecessary(throwable);
    }
  }

  public List<Class<?>> listDrivers(String packagePrefix) {
    return ReflectionUtils.allTypesAnnotatedWith(packagePrefix, RunWith.class)
        .filter(aClass -> aClass.getAnnotation(RunWith.class).value().equals(ScriptiveUnit.class))
        .collect(toList());
  }

  public List<String> listRunners() {
    return Arrays.stream(RunningMode.values()).map((RunningMode mode) -> StringUtils.toCamelCase(mode.name())).collect(toList());
  }

  public List<Class<?>> listSuiteSets(String packagePrefix) {
    return ReflectionUtils.allTypesAnnotatedWith(packagePrefix, RunWith.class)
        .filter(aClass -> aClass.getAnnotation(RunWith.class).value().equals(ScriptiveSuiteSet.class))
        .collect(toList());
  }

  public List<String> listScripts(Class<?> suiteSetClass) {
    return new SuiteScripts.Streamer(validateSuiteSetClass(suiteSetClass).getAnnotation(SuiteScripts.class)).stream().collect(toList());
  }

  public Result runSuiteSet(Class<?> suiteSetClass) {
    return JUnitCore.runClasses(suiteSetClass);
  }

  public Result runScript(Class<?> driverClass, String scriptResourceName) {
    try {
      return new JUnitCore().run(new ScriptiveUnit(driverClass, new JsonScript.Standard(driverClass, new Properties(), scriptResourceName)));
    } catch (Throwable throwable) {
      throw ScriptiveUnitException.wrapIfNecessary(throwable);
    }
  }

  enum Utils {
    ;

    public static Description describeForm(ScriptiveUnit scriptiveUnit, Object driverObject, String formName) {
      Optional<Description> value =
          Stream.concat(
              DriverUtils.getFormsFromImportedFieldsInObject(driverObject).stream().map(Form::describe),
              Private.getUserDefinedFormClauses(scriptiveUnit).entrySet().stream().map((Map.Entry<String, List<Object>> entry) -> Description.describe(entry.getKey(), entry.getValue()))
          ).filter(t -> formName.equals(t.name())).findFirst();
      if (value.isPresent())
        return value.get();
      throw functionNotFound(formName);
    }

    public static List<String> getFormNames(ScriptiveUnit scriptiveUnit, Object driverObject) {
      return Stream.concat(
          DriverUtils.getFormsFromImportedFieldsInObject(driverObject)
              .stream()
              .map(Form::getName),
          Private.getUserDefinedFormClauseNamesFromScript(scriptiveUnit).stream()).collect(toList());
    }

    enum Private {
      ;

      private static List<String> getUserDefinedFormClauseNamesFromScript(ScriptiveUnit scriptiveUnit) {
        return new ArrayList<>(getUserDefinedFormClauses(scriptiveUnit).keySet());
      }

      private static Map<String, List<Object>> getUserDefinedFormClauses(ScriptiveUnit scriptiveUnit) {
        return scriptiveUnit.getTestSuiteDescriptor().getUserDefinedFormClauses();
      }
    }
  }
}
