package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.model.lang.LanguageSpec;

import java.util.Objects;
import java.util.Optional;

public interface Script<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
  Optional<Reporting> getReporting();

  LanguageSpec<NODE, OBJECT, ARRAY, ATOM> languageSpec();

  OBJECT mainNode();

  default String name() {
    return Objects.toString(this.getClass().getCanonicalName());
  }
}
