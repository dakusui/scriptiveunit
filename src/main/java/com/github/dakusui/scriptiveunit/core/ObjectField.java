package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;

import java.lang.reflect.Field;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public interface ObjectField {
  Object get();

  Field getField();

  /**
   * Creates a new {@code ObjectField} instance.
   *
   * @param object An object to which field belongs.
   * @param field  A field of an {@code object}.
   */
  static ObjectField create(Object object, Field field) {
    checkArgument(requireNonNull(field).getDeclaringClass().isAssignableFrom(requireNonNull(object).getClass()));
    return new ObjectField() {
      @Override
      public Object get() {
        return ReflectionUtils.getFieldValue(object, field);
      }

      @Override
      public Field getField() {
        return field;
      }
    };
  }
}
