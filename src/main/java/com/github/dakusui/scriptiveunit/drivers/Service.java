package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Form;

import java.util.Map;

public abstract class Service<REQUEST, RESPONSE> extends Core {
  @SuppressWarnings("unused")
  @Scriptable
  public Form<RESPONSE> service(Form<REQUEST> request) {
    return (Stage input) -> Service.this.service(request.apply(input));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<REQUEST> with(Form<Map<String, Object>> values, Form<REQUEST> request) {
    return (Stage input) -> override(values.apply(input), request.apply(input));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<REQUEST> request() {
    return (Stage input) -> buildRequest(input.getTestCaseTuple());
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<RESPONSE> response() {
    //noinspection unchecked
    return (Stage input) -> (RESPONSE) input.response();
  }

  /**
   * @param fixture A test case.
   */
  abstract protected REQUEST buildRequest(Map<String, Object> fixture);

  abstract protected RESPONSE service(REQUEST request);

  abstract protected REQUEST override(Map<String, Object> values, REQUEST request);

}
