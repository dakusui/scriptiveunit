package com.github.dakusui.scriptiveunit.drivers;

import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.Stage;

import java.util.Map;

public abstract class Service<REQUEST, RESPONSE> extends Core {
  @ReflectivelyReferenced
  @Scriptable
  public Func.Memoized<RESPONSE> service(Func<REQUEST> request) {
    return (Stage input) -> Service.this.service(request.apply(input));
  }

  @ReflectivelyReferenced
  @Scriptable
  public Func<REQUEST> with(Func<Map<String, Object>> values, Func<REQUEST> request) {
    return (Stage input) -> override(values.apply(input), request.apply(input));
  }

  @ReflectivelyReferenced
  @Scriptable
  public Func<REQUEST> request() {
    return (Stage input) -> buildRequest(input.getTestCaseTuple());
  }

  @ReflectivelyReferenced
  @Scriptable
  public Func<RESPONSE> response() {
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
