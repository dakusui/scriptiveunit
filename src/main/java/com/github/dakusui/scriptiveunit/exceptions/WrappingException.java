package com.github.dakusui.scriptiveunit.exceptions;

class WrappingException extends ScriptiveUnitException {
  WrappingException(String message, Throwable nested) {
    super(message, nested);
  }
}
