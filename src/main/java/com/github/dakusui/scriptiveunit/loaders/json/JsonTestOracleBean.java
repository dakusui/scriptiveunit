package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.loaders.beans.TestOracleBean;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class JsonTestOracleBean extends TestOracleBean {
  @JsonCreator
  public JsonTestOracleBean(
      @JsonProperty("description") String description,
      @JsonProperty("before") List<Object> before,
      @JsonProperty("given") List<Object> given,
      @JsonProperty("when") List<Object> when,
      @JsonProperty("then") List<Object> then,
      @JsonProperty("onFailure") List<Object> onFailure,
      @JsonProperty("after") List<Object> after
  ) {
    super(description, before, given, when, then, onFailure, after);
  }
}
