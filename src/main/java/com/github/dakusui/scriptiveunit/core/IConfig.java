package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.HostSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;

public interface IConfig<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
  default Preprocessor createPreprocessor() {
    return new Preprocessor.Builder<>(createHostSpec())
        .applicationSpec(createApplicationSpec())
        .build();
  }

  ApplicationSpec.Dictionary readScriptResource();

  ApplicationSpec.Dictionary readRawScriptResource(String s, HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec);

  ApplicationSpec createApplicationSpec();

  HostSpec<NODE, OBJECT, ARRAY, ATOM> createHostSpec();

  String name();
}
