package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.faultsource.FaultSource;

public enum Exceptions implements FaultSource {
  I {
    @Override
    public RuntimeException exceptionForCaughtFailure(String message, Throwable t) {
      throw new RuntimeException(message, t);
    }

    @Override
    public RuntimeException exceptionForIllegalValue(String message) {
      return new IllegalArgumentException(message);
    }
  };
}
