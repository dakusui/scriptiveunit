package com.github.dakusui.scriptiveunit.testutils.drivers;

import com.github.dakusui.scriptiveunit.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Import.Alias;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.drivers.*;
import com.github.dakusui.scriptiveunit.drivers.actions.Basic;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBasedLoader;
import org.junit.runner.RunWith;

@Load(with = JsonBasedLoader.class)
@RunWith(ScriptiveUnit.class)
public class Simple {
  @ReflectivelyReferenced
  @Import({
      @Alias(value = "*"),
      @Alias(value = "add", as = "+"),
      @Alias(value = "sub", as = "-"),
      @Alias(value = "mul", as = "*"),
      @Alias(value = "div", as = "/")
  })
  public Object arith = new Arith();

  @ReflectivelyReferenced
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

  @ReflectivelyReferenced
  @Import
  public Object strings = new Strings();

  @ReflectivelyReferenced
  @Import
  public Object collections = new Collections();

  @ReflectivelyReferenced
  @Import
  public Object basicActions = new Basic();

  @ReflectivelyReferenced
  @Import
  public Object core = new Core();

  @ReflectivelyReferenced
  @Import
  public Object reporting = new Reporting();


  @RunWith(ScriptiveSuiteSet.class)
  @ScriptiveSuiteSet.SuiteScripts(driverClass = Simple.class, includes = ".*reporting\\.json")
  public static class Run {

  }
}
