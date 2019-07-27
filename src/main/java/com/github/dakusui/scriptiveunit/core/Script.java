package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.LanguageSpec;
import com.github.dakusui.scriptiveunit.model.lang.ResourceStoreSpec;

import java.util.Objects;
import java.util.Optional;

public interface Script<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
  default Preprocessor createPreprocessor() {
    return Preprocessor.create(languageSpec().hostSpec(), languageSpec().applicationSpec());
  }

  Optional<Reporting> getReporting();

  ApplicationSpec.Dictionary readScriptResource(ResourceStoreSpec resourceStoreSpec, OBJECT script);

  LanguageSpec<NODE, OBJECT, ARRAY, ATOM> languageSpec();

  default String name() {
    return Objects.toString(this.getClass().getCanonicalName());
  }
}
