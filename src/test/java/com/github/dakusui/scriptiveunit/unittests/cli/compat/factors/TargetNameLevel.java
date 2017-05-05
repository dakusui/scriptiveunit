package com.github.dakusui.scriptiveunit.unittests.cli.compat.factors;

public enum TargetNameLevel {

  VALID(true) {
  },
  NOT_FOUND(false) {
  },
  INVALID(false) {
  },
  MISSING(false) {
  };

  final private boolean valid;

  TargetNameLevel(boolean valid) {
    this.valid = valid;
  }
}
