package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.libs.Core;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.Strings;
import com.github.dakusui.scriptiveunit.libs.extras.DataStore;
import com.github.dakusui.scriptiveunit.libs.extras.SearchEngineSupport;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;

public abstract class SureSearchTestBase {
  @Import
  public final Core                                                                                   core                = new Core();
  @Import
  public final Strings                                                                                strings             = new Strings();
  @Import
  public final Predicates                                                                             predicates          = new Predicates();
  @Import
  public final SearchEngineSupport<SureSearchRequest, SureSearchResponse, ApplicationSpec.Dictionary> searchEngineSupport =
      new SearchEngineSupport<>(
          new SureSearch(SureSearchDocSet.DEFAULT.docs()),
          new SureSearchResultEvaluator(SureSearchDocSet.DEFAULT));
  @Import
  public final DataStore                                                                              dataStore           = new DataStore();
}
