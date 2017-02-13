package com.github.dakusui.scriptunit.loaders.json;

import com.github.dakusui.scriptunit.loaders.Beans;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import java.util.Map;

public enum JsonBeans {
  ;

  public static class TestSuiteDescriptorBean extends Beans.BaseForTestSuiteDescriptor {
    public TestSuiteDescriptorBean(
        @JsonProperty("description") String description,
        @JsonProperty("coveringArrayEngine") CoveringArrayEngineConfigBean coveringArrayEngineConfig,
        @JsonProperty("factorSpace") FactorSpaceDescriptorBean factorSpaceBean,
        @JsonProperty("runnerType") String runnerType,
        @JsonProperty("setUpBeforeAll") List<Object> setUpBeforeAllClause,
        @JsonProperty("setUp") List<Object> setUpClause,
        @JsonProperty("testOracles") List<TestOracleBean> testOracleBeanList
    ) {
      super(coveringArrayEngineConfig, factorSpaceBean, setUpBeforeAllClause, setUpClause, testOracleBeanList, description, runnerType);
    }

    public static class CoveringArrayEngineConfigBean extends Beans.BaseForCoveringArrayEngineConfig {
      public CoveringArrayEngineConfigBean(
          @JsonProperty("class") String className,
          @JsonProperty("options") List<Object> options) {
        super(className, options);
      }
    }

    public static class FactorSpaceDescriptorBean extends Beans.BaseForFactorSpaceDescriptor {
      public FactorSpaceDescriptorBean(
          @JsonProperty("factors") Map<String, List<Object>> factorMap,
          @JsonProperty("constraints") List<List<Object>> constraintList
      ) {
        super(factorMap, constraintList);
      }
    }

    public static class TestOracleBean extends Beans.BaseForTestOracle {
      @JsonCreator
      public TestOracleBean(
          @JsonProperty("description") String description,
          @JsonProperty("given") List<Object> given,
          @JsonProperty("when") List<Object> when,
          @JsonProperty("then") List<Object> then
      ) {
        super(description, given, when, then);
      }
    }
  }
}
