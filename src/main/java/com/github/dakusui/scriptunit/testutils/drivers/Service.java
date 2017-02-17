package com.github.dakusui.scriptunit.testutils.drivers;

import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.annotations.Scriptable;
import com.github.dakusui.scriptunit.model.func.Func;
import com.github.dakusui.scriptunit.model.Stage;

import java.util.Map;

public abstract class Service<REQUEST, RESPONSE> extends Core {
  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage> Func.Memoized<T, RESPONSE> service(Func<T, REQUEST> request) {
    return (T input) -> Service.this.service(request.apply(input));
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage> Func<T, REQUEST> with(Func<T, Map<String, Object>> values, Func<T, REQUEST> request) {
    return (T input) -> override(values.apply(input), request.apply(input));
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage> Func<T, REQUEST> request() {
    return (T input) -> buildRequest(input.getTestCaseTuple());
  }

  @ReflectivelyReferenced
  @Scriptable
  public <T extends Stage> Func<T, RESPONSE> response() {
    //noinspection unchecked
    return (T input) -> (RESPONSE) input.response();
  }

  /**
   * @param fixture A test case.
   */
  abstract protected REQUEST buildRequest(Map<String, Object> fixture);

  abstract protected RESPONSE service(REQUEST request);

  abstract protected REQUEST override(Map<String, Object> values, REQUEST request);

}
