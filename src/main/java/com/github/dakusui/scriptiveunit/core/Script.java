package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.lang.LanguageSpec;

import java.util.Objects;
import java.util.Optional;

public interface Script<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
  default Preprocessor createPreprocessor() {
    return Preprocessor.create(hostSpec(), applicationSpec());
  }

  Optional<Reporting> getReporting();

  ApplicationSpec.Dictionary readRawBaseScript();

  ApplicationSpec.Dictionary readScriptResource();

  ApplicationSpec.Dictionary readRawScriptResource(String s, HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec);

  LanguageSpec<NODE, OBJECT, ARRAY, ATOM> languageSpec();

  default ApplicationSpec applicationSpec() {
    return languageSpec().applicationSpec();
  }

  default HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec() {
    return languageSpec().hostSpec();
  }

  default String name() {
    return Objects.toString(this.getClass().getCanonicalName());
  }
}
