package com.github.dakusui.scriptunit.drivers;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptunit.annotations.AccessesTestParameter;
import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.annotations.Scriptable;
import com.github.dakusui.scriptunit.exceptions.ScriptUnitException;
import com.github.dakusui.scriptunit.model.func.Func;
import com.github.dakusui.scriptunit.model.Stage;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static com.github.dakusui.scriptunit.core.Utils.check;
import static com.github.dakusui.scriptunit.exceptions.SyntaxException.attributeNotFound;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class Core {
  /**
   * Returns a function to access an attribute value in a test case.
   * This can be used in {@code GENERATION}, {@code GIVEN}, {@code WHEN}, and {@code THEN}
   * stages.
   *
   * @param attr Attribute name whose value should be returned
   * @param <T>  Type of stage
   * @param <E>  Type of attribute value to be returned.
   */
  @ReflectivelyReferenced
  @Scriptable
  @AccessesTestParameter
  public <T extends Stage, E> Func<T, E> attr(Func<T, String> attr) {
    return (T input) -> {
      Tuple fixture = input.getTestCaseTuple();
      String attrName = attr.apply(input);
      check(
          fixture.containsKey(attrName),
          attributeNotFound(attrName, input.getType().toString().toLowerCase(), fixture.keySet()));
      //noinspection unchecked
      return (E) fixture.get(attrName);
    };
  }

  /**
   * Returns a function to invoke a method of a specified name. The returned value
   * of the method will be returned as the function's value.
   *
   * @param entryName A name of method to be invoked.
   * @param <T>       Type of stage
   * @param <E>       Type of the function's value to be returned.
   */
  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage, E> Func<T, E> value(Func<T, String> entryName, Func<T, ?> target) {
    return (T input) -> {
      Object object = requireNonNull(target.apply(input));
      String methodName = requireNonNull(entryName.apply(input));
      try {
        //noinspection unchecked
        return (E) object.getClass().getMethod(methodName).invoke(object);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        throw ScriptUnitException.wrap(e);
      }
    };
  }

  @ReflectivelyReferenced
  @SafeVarargs
  @Scriptable
  public final <T extends Stage> Func<T, List<?>> quote(Func<T, ?>... values) {
    return (T input) -> Arrays
        .stream(values)
        .map((Func<T, ?> each) -> each instanceof Func.Const ? each.apply(input) : each)
        .collect(toList());
  }
}
