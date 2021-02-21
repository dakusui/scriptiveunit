package com.github.dakusui.scriptiveunit.unittests.utils;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.libs.Predicates;

public class DriverUtilsTest {
  public static class DriverExample {
    @Import({
        @Import.Alias(value = "*"),
        @Import.Alias(value = "gt", as = ">"),
        @Import.Alias(value = "ge", as = ">="),
        @Import.Alias(value = "lt", as = "<"),
        @Import.Alias(value = "le", as = "<="),
        @Import.Alias(value = "eq", as = "=="),
        @Import.Alias(value = "ifthen", as = "if_then")
    })
    public final Object predicates = new Predicates();
  }
}
