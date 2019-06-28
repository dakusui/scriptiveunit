package com.github.dakusui.scriptiveunit.testassets.drivers;

import com.github.dakusui.scriptiveunit.drivers.contrib.Reporting;
import com.github.dakusui.scriptiveunit.utils.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Import.Alias;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.drivers.*;
import com.github.dakusui.scriptiveunit.drivers.actions.Basic;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBasedTestSuiteDescriptorLoader;
import org.junit.runner.RunWith;

@Load(with = JsonBasedTestSuiteDescriptorLoader.class)
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


  @RunWith(ScriptiveSuiteSet.class)
  @ScriptiveSuiteSet.SuiteScripts(driverClass = Simple.class, includes = ".*reporting\\.json")
  public static class Run {

  }
}
