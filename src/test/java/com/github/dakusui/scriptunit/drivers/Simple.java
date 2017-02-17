package com.github.dakusui.scriptunit.drivers;

import com.github.dakusui.scriptunit.ScriptUnit;
import com.github.dakusui.scriptunit.annotations.Import;
import com.github.dakusui.scriptunit.annotations.Import.Alias;
import com.github.dakusui.scriptunit.annotations.Load;
import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.drivers.actions.Basic;
import com.github.dakusui.scriptunit.loaders.json.JsonBasedTestSuiteLoader;
import org.junit.runner.RunWith;

@Load(
    scriptPackagePrefix = "tests", scriptNamePattern = ".*\\.json", with = JsonBasedTestSuiteLoader.Factory.class)
@RunWith(ScriptUnit.class)
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
}
