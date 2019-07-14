package com.github.dakusui.scriptiveunit.testassets.drivers;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Import.Alias;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.libs.Arith;
import com.github.dakusui.scriptiveunit.libs.Collections;
import com.github.dakusui.scriptiveunit.libs.Core;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.Strings;
import com.github.dakusui.scriptiveunit.libs.actions.Basic;
import com.github.dakusui.scriptiveunit.libs.extras.Reporting;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.runners.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.runners.ScriptiveSuiteSet.SuiteScripts;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.junit.runner.RunWith;

import java.util.Map;

@Load()
@RunWith(ScriptiveUnit.class)
public class Simple {
  @SuppressWarnings("unused")
  @Import({
      @Alias(value = "*"),
      @Alias(value = "add", as = "+"),
      @Alias(value = "sub", as = "-"),
      @Alias(value = "mul", as = "*"),
      @Alias(value = "div", as = "/")
  })
  public Object arith = new Arith();

  @SuppressWarnings("unused")
  @Import({
      @Alias(value = "*"),
      @Alias(value = "gt", as = ">"),
      @Alias(value = "ge", as = ">="),
      @Alias(value = "lt", as = "<"),
      @Alias(value = "le", as = "<="),
      @Alias(value = "eq", as = "=="),
      @Alias(value = "ifthen", as = "if_then")
  })
  public Object predicates = new Predicates();

  @SuppressWarnings("unused")
  @Import
  public Object strings = new Strings();

  @SuppressWarnings("unused")
  @Import
  public Object collections = new Collections();

  @SuppressWarnings("unused")
  @Import
  public Object basicActions = new Basic();

  @SuppressWarnings("unused")
  @Import
  public Object core = new Core();

  @SuppressWarnings("unused")
  @Import
  public Object reporting = new Reporting();

  @SuppressWarnings("unused")
  @Import({
      @Alias(value = "request", as = "query"),
      @Alias(value = "response", as = "result"),
      @Alias(value = "service", as = "issue"),
      @Alias(value = "override", as = "with")
  })
  public Object additinal = new Additional();

  @SuppressWarnings("WeakerAccess")
  public static class Additional {
    @SuppressWarnings("unused")
    @Scriptable
    public Value<String> request() {
      return (Stage input) -> buildRequest(input.getTestCaseTuple().orElseThrow(RuntimeException::new));
    }

    @SuppressWarnings("unused")
    @Scriptable
    public Value<String> response() {
      return (Stage input) -> (String) input.response().orElseThrow(RuntimeException::new);
    }

    @SuppressWarnings("unused")
    @Scriptable
    public Value<String> service(Value<String> request) {
      return (Stage input) -> Additional.this.service(request.apply(input));
    }

    @SuppressWarnings("unused")
    @Scriptable
    public Value<String> override(Value<Map<String, Object>> values, Value<String> request) {
      return (Stage input) -> override(values.apply(input), request.apply(input));
    }

    /**
     * @param fixture A test case.
     */
    protected String buildRequest(Map<String, Object> fixture) {
      return String.format("request:[%s]", fixture);
    }

    protected String service(String request) {
      return String.format("response:[%s]", request);
    }

    protected String override(Map<String, Object> values, String request) {
      return String.format("override:[%s, [%s]}", request, values);
    }
  }

  @RunWith(ScriptiveSuiteSet.class)
  @SuiteScripts(driverClass = Simple.class, includes = ".*reporting\\.json")
  public static class Run {
  }
}
