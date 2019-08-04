package com.github.dakusui.scriptiveunit.libs;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.scriptiveunit.annotations.AccessesTestParameter;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.exceptions.Exceptions;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;
import com.github.dakusui.scriptiveunit.model.stage.Stage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static com.github.dakusui.scriptiveunit.exceptions.Exceptions.attributeNotFound;
import static com.github.dakusui.scriptiveunit.exceptions.Exceptions.systemAttributeNotFound;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;
import static com.github.dakusui.scriptiveunit.utils.ReflectionUtils.chooseMethod;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class Core {
  /**
   * Returns a function to access an attribute value in a test case.
   * This can be used in {@code SUITE}, {@code FIXTURE}, and {@code ORACLE}
   * stages.
   *
   * @param attr Attribute name whose value should be returned
   * @param <E>  Type of attribute value to be returned.
   */
  @SuppressWarnings({ "unused", "unchecked" })
  @Scriptable
  @AccessesTestParameter
  public <E> Value<E> attr(Value<String> attr) {
    return (Stage input) -> {
      Tuple testCase = input.getTestCaseTuple().orElseThrow(RuntimeException::new);
      String attrName = attr.apply(input);
      check(
          testCase.containsKey(attrName),
          () -> attributeNotFound(attrName, input, testCase.keySet()));
      return (E) testCase.get(attrName);
    };
  }

  /**
   * Returns a function to invoke a method of a specified name. The returned value
   * of the method will be returned as the function's value.
   *
   * @param <E>       Type of the function's value to be returned.
   * @param entryName A name of method to be invoked.
   * @param target    A target from which value of {@code entryName} will be returned.
   */
  @SuppressWarnings({ "unused", "unchecked" })
  @Scriptable
  public <E> Value<E> value(Value<String> entryName, Value<?> target, ValueList<?> argList) {
    return (Stage input) -> {
      Object object = requireNonNull(target.apply(input));
      String methodName = requireNonNull(entryName.apply(input));
      Object[] args = argList.stream()
          .map(arg -> arg.apply(input))
          .toArray();
      try {
        return (E) chooseMethod(Arrays.stream(object.getClass().getMethods())
            .filter(m -> m.getName().equals(methodName))
            .toArray(Method[]::new), args)
            .invoke(object, (Object[]) args);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw ScriptiveUnitException.wrapIfNecessary(e);
      }
    };
  }

  @Scriptable
  public Value<Throwable> exception() {
    return stage -> stage.getThrowable()
        .orElseThrow(IllegalStateException::new);
  }

  @Scriptable
  public Value<TestCase> testCase() {
    return stage -> stage.getTestCase().orElseThrow(
        () -> new IllegalStateException(
            format("This method cannot be called on this stage:<%s>", stage)));
  }

  @Scriptable
  public Value<TestOracle> testOracle() {
    return stage -> stage.getTestOracle().orElseThrow(
        () -> new IllegalStateException(
            format("This method cannot be called on this stage:<%s>", stage)));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Value<List<?>> quote(ValueList<?> values) {
    return (Stage input) -> values
        .stream()
        .map((Value<?> each) -> each instanceof Value.Const ? each.apply(input) : each)
        .collect(toList());
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Value<Object> scriptAttr(Value<String> attrName) {
    return input -> {
      String attr = requireNonNull(attrName.apply(input));
      Script work = input.getScript();
      if (!(work instanceof JsonScript.FromDriverClass))
        throw Exceptions.nonStandardScript(work);
      JsonScript.FromDriverClass script = (JsonScript.FromDriverClass) work;
      final Object retValue;
      switch (attr) {
      case "driverClass":
        retValue = script.getTestClass().getCanonicalName();
        break;
      case "scriptResourceName":
        retValue = script.getScriptResourceName();
        break;
      case "scriptResourceNameKey":
        retValue = script.getScriptResourceNameKey();
        break;
      default:
        throw systemAttributeNotFound(attr, input);
      }
      return retValue;
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Value<Object> systemProperty(Value<String> attrName) {
    return input -> System.getProperties().getProperty(requireNonNull(attrName.apply(input)));
  }

  @SuppressWarnings("unchecked")
  @Scriptable
  public <T> Value<T> output() {
    return input -> Stage.evaluateValue(input, i -> (T) i.response().orElseThrow(RuntimeException::new));
  }
}
