package com.github.dakusui.scriptiveunit.unittests.cli.compat.factors;

import com.github.dakusui.scriptiveunit.unittests.cli.SimpleDriver;
import com.github.dakusui.scriptiveunit.unittests.cli.SuiteSetDriver;

public enum DriverLevel {
  @SuppressWarnings("unused")
  SUITESET(SuiteSetDriver.class),
  @SuppressWarnings("unused")
  SIMPLE(SimpleDriver.class),
  @SuppressWarnings("unused")
  NOT_FOUND(false, "not_found"),
  @SuppressWarnings("unused")
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
