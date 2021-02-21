package com.github.dakusui.scriptiveunit.examples.searchengine.examples;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Expect {
  int passing();
  int failing();
  int ignored();
}
