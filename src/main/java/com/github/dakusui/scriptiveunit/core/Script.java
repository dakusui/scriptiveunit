package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.HostSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.form.FormRegistry;

import java.util.Optional;

public interface Script<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
  default Preprocessor createPreprocessor() {
    return new Preprocessor.Builder<>(createHostSpec())
        .applicationSpec(createApplicationSpec())
        .build();
  }

  FormRegistry formRegistry();

  Optional<Reporting> getReporting();

  ApplicationSpec.Dictionary readRawBaseScript();

  ApplicationSpec.Dictionary readScriptResource();

  ApplicationSpec.Dictionary readRawScriptResource(String s, HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec);

  ApplicationSpec createApplicationSpec();

  HostSpec<NODE, OBJECT, ARRAY, ATOM> createHostSpec();

  default String name() {
    return this.getClass().getCanonicalName();
  }
}
