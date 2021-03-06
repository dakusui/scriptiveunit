package com.github.dakusui.scriptiveunit.drivers.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.drivers.Core;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.form.Form;

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
    return (Stage input) -> buildRequest(input.getTestCaseTuple().orElseThrow(RuntimeException::new));
  }

  @SuppressWarnings("unused")
  @Scriptable
  public Form<RESPONSE> response() {
    //noinspection unchecked
    return (Stage input) -> (RESPONSE) input.response().orElseThrow(RuntimeException::new);
  }

  /**
   * @param fixture A test case.
   */
  abstract protected REQUEST buildRequest(Map<String, Object> fixture);

  abstract protected RESPONSE service(REQUEST request);

  abstract protected REQUEST override(Map<String, Object> values, REQUEST request);

}
