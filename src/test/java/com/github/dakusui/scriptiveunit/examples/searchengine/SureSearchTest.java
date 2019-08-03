package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.annotations.CompileWith;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.libs.extras.SearchEngineLib;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.junit.runner.RunWith;

/**
 * A search engine named "SureSearch", which is for explaining "ScriptiveUnit"'s functionality.
 */
@RunScript(compiler = @CompileWith)
@RunWith(ScriptiveUnit.class)
public class SureSearchTest {

  @Import
  public SearchEngineLib<SureSearchRequest, SureSearchResponse, ApplicationSpec.Dictionary> searchEngineLib =
      new SearchEngineLib<>(new SureSearch(), new SureSearchResultEvaluator());

}
