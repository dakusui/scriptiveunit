package com.github.dakusui.scriptiveunit.tests.cli.compat.factors;

import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.tests.cli.SimpleDriver;
import com.github.dakusui.scriptiveunit.tests.cli.SuiteSetDriver;

public enum DriverLevel {
  @ReflectivelyReferenced
  SUITESET(SuiteSetDriver.class),
  @ReflectivelyReferenced
  SIMPLE(SimpleDriver.class),
  @ReflectivelyReferenced
  NOT_FOUND(false, "not_found"),
  @ReflectivelyReferenced
  INVALID_NAME(false, "!+-");

  final private String  driverClassName;
  final private boolean valid;

  DriverLevel(Class<?> driverClass) {
    this(true, driverClass.getCanonicalName());
  }


  DriverLevel(boolean valid, String driverClassName) {
    this.driverClassName = driverClassName;
    this.valid = valid;
  }

  public String getDriverClassName() {
    return this.driverClassName;
  }
}
