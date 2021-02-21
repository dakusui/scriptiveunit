package com.github.dakusui.scriptiveunit.exceptions;

import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import org.codehaus.jackson.JsonNode;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public enum Exceptions {
  ;

  public static ResourceException scriptNotFound(String scriptName) {
    return new ResourceException(format("Script:<%s> was not found.", scriptName));
  }

  public static ResourceException functionNotFound(String functionName) {
    throw new ResourceException(String.format("A function:<%s> was not found.", functionName));
  }

  public static ScriptiveUnitException indexOutOfBounds(int index, int size) {
    return new ScriptiveUnitUnclassifiedException(format("%sth element was accessed but the container's length was %s", index, size));
  }

  public static ScriptiveUnitException noScriptResourceNameKeyWasGiven() {
    throw new ScriptiveUnitUnclassifiedException("No script resource name key was given");
  }

  public static ScriptiveUnitException noReportingObjectIsAvailable() {
    throw new ScriptiveUnitUnclassifiedException("No reporting object is available in this session.");
  }

  public static SyntaxException attributeNotFound(String attributeName, Stage context, Iterable<String> knownAttributeNames) {
    throw new ScriptiveUnitUnclassifiedException(format(
        "Attribute '%s' is accessed in stage:<%s>, but not found in your test case. Known attribute names are %s'",
        attributeName,
        context,
        knownAttributeNames));
  }

  public static SyntaxException nonObject(JsonNode jsonNode) {
    throw new SyntaxException(format("An object node was expected but not. '%s'", jsonNode));
  }

  public static <E extends ScriptiveUnitException> E nonArray(JsonNode jsonNode) {
    throw new SyntaxException(format("An array node was expected but not. '%s'", jsonNode));
  }

  public static <E extends ScriptiveUnitException> E nonText(JsonNode jsonNode) {
    throw new SyntaxException(format("A text node was expected but not. '%s'", jsonNode));
  }

  public static SyntaxException parameterNameShouldBeSpecifiedWithConstant(Statement.Compound statement) {
    throw new SyntaxException(format("Parameter name must be constant but not when accessor is used. (%s %s)", statement.getFormHandle(), statement.getArguments()));
  }

  public static SyntaxException cyclicTemplatingFound(String context, Map<String, Object> map) {
    throw new SyntaxException(format("Cyclic templating was detected in %s (%s)", context, map));
  }

  public static SyntaxException undefinedFactor(String factorName, Object context) {
    throw new SyntaxException(format("Undefined factor name '%s' was used in %s", factorName, context));
  }

  public static SyntaxException systemAttributeNotFound(String attr, Stage input) {
    throw new SyntaxException(format("Unknown system attribute '%s' was accessed in '%s'", attr, input));
  }

  public static TypeMismatch valueReturnedByScriptableMethodWasNotValueObject(String methodName, Object returnedValue) {
    throw new TypeMismatch("Value '%s' returned by '%s' must be an instance of '%s'", returnedValue, methodName, Value.class.getCanonicalName());
  }

  public static TypeMismatch headOfCallMustBeString(Object car) {
    throw new TypeMismatch("Head of a call must be a string but '%s' as given", car);
  }

  public static ConfigurationException duplicatedFormsAreFound(Map<String, List<Form>> duplicatedObjectMethods, Class<?> driverClass) {
    StringBuffer buf = new StringBuffer();
    duplicatedObjectMethods.forEach((s, objectMethods) -> {
      buf.append(format("Alias:<%s>:%n", s));
      objectMethods.forEach((Form each) -> buf.append(format("  %s%n", each)));
      buf.append(format("%n"));
    });
    String found = buf.toString();
    throw new ConfigurationException(format(
        "Following object methods are found duplicated in class:<%s>:%n%s", driverClass.getCanonicalName(), found
    ));
  }

  public static ScriptiveUnitException nonStandardScript(Script script) {
    throw new ScriptiveUnitUnclassifiedException(format("Non-standard config:<%s> was given", script));
  }

  public static ConfigurationException noScriptCompilerProvided(Class<?> driverClass) {
    throw new ConfigurationException(format("No compiler was provided for class:<%s>", driverClass));
  }

  public static ConfigurationException noScriptLoaderProvided(Class<?> driverClass) {
    throw new ConfigurationException(format("No script loader was provided for class:<%s>", driverClass));
  }

  public static ScriptiveUnitException unclassifiedException(String format, Object... args) {
    return new ScriptiveUnitUnclassifiedException(format(format, args));
  }
}
