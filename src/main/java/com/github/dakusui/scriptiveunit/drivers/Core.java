package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.annotations.AccessesTestParameter;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.exceptions.SyntaxException;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestItem;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static com.github.dakusui.scriptiveunit.core.Utils.check;
import static com.github.dakusui.scriptiveunit.exceptions.SyntaxException.attributeNotFound;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class Core {
  /**
   * Returns a function to access an attribute value in a test case.
   * This can be used in {@code GENERATION}, {@code GIVEN}, {@code WHEN}, and {@code THEN}
   * stages.
   *
   * @param attr Attribute name whose value should be returned
   * @param <E>  Type of attribute value to be returned.
   */
  @SuppressWarnings("unused")
  @Scriptable
  @AccessesTestParameter
  public <E> Func<E> attr(Func<String> attr) {
    return (Stage input) -> {
      Tuple testCase = input.getTestCaseTuple();
      String attrName = attr.apply(input);
      check(
          testCase.containsKey(attrName),
          attributeNotFound(attrName, input, testCase.keySet()));
      //noinspection unchecked
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
  @SuppressWarnings("unused")
  @Scriptable
  public <E> Func<E> value(Func<String> entryName, Func<?> target) {
    return (Stage input) -> {
      Object object = requireNonNull(target.apply(input));
      String methodName = requireNonNull(entryName.apply(input));
      try {
        //noinspection unchecked
        return (E) object.getClass().getMethod(methodName).invoke(object);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        throw ScriptiveUnitException.wrap(e);
      }
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Func<Throwable> exception() {
    return Stage::getThrowable;
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Func<TestItem> testItem() {
    return Stage::getTestItem;
  }

  @SuppressWarnings("unused")
  @Scriptable
  public final Func<List<?>> quote(Func<?>... values) {
    return (Stage input) -> Arrays
        .stream(values)
        .map((Func<?> each) -> each instanceof Func.Const ? each.apply(input) : each)
        .collect(toList());
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Func<Object> configAttr(Func<String> attrName) {
    return input -> {
      String attr = requireNonNull(attrName.apply(input));
      Config config = input.getConfig();
      final Object retValue;
      switch (attr) {
      case "driverClass":
        retValue = config.getDriverClass().getCanonicalName();
        break;
      case "scriptResourceName":
        retValue = config.getScriptResourceName();
        break;
      case "scriptResourceNameKey":
        retValue = config.getScriptResourceNameKey();
        break;
      default:
        throw SyntaxException.systemAttributeNotFound(attr, input);
      }
      return retValue;
    };
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Func<Object> systemProperty(Func<String> attrName) {
    return input -> System.getProperties().getProperty(requireNonNull(attrName.apply(input)));
  }
}
