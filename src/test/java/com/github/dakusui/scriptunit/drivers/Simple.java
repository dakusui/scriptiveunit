package com.github.dakusui.scriptunit.drivers;

import com.github.dakusui.scriptunit.ScriptiveUnit;
import com.github.dakusui.scriptunit.annotations.Import;
import com.github.dakusui.scriptunit.annotations.Import.Alias;
import com.github.dakusui.scriptunit.annotations.Load;
import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.loaders.json.JsonBasedTestSuiteLoader;
import com.github.dakusui.scriptunit.testutils.drivers.Arith;
import com.github.dakusui.scriptunit.testutils.drivers.Collections;
import com.github.dakusui.scriptunit.testutils.drivers.Predicates;
import com.github.dakusui.scriptunit.testutils.drivers.Strings;
import com.github.dakusui.scriptunit.testutils.drivers.actions.Basic;
import org.junit.runner.RunWith;

@Load(
    scriptPackagePrefix = "tests", scriptNamePattern = ".*\\.json", with = JsonBasedTestSuiteLoader.Factory.class)
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
}
