package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.loaders.Beans;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public enum JsonBeans {
  ;

  public static class TestSuiteDescriptorBean extends Beans.BaseForTestSuiteDescriptor {
    public TestSuiteDescriptorBean(
        @JsonProperty("description") String description,
        @JsonProperty("factorSpace") FactorSpaceDescriptorBean factorSpaceBean,
        @JsonProperty("runnerType") String runnerType,
        @JsonProperty("define") Map<String, List<Object>> userFormMap,
        @JsonProperty("setUpBeforeAll") List<Object> setUpBeforeAllClause,
        @JsonProperty("setUp") List<Object> setUpClause,
        @JsonProperty("testOracles") List<TestOracleBean> testOracleBeanList,
        @JsonProperty("tearDown") List<Object> tearDownClause,
        @JsonProperty("tearDownAfterAll") List<Object> tearDownAfterAllClause
    ) {
      super(description,
          factorSpaceBean,
          runnerType,
          userFormMap,
          setUpBeforeAllClause,
          setUpClause,
          testOracleBeanList,
          tearDownClause,
          tearDownAfterAllClause);
    }

    public static class FactorSpaceDescriptorBean extends Beans.BaseForFactorSpaceDescriptor {
      public FactorSpaceDescriptorBean(
          @JsonProperty("factors") Map<String, Map<String, Object>> parameterMap,
          @JsonProperty("constraints") List<List<Object>> constraintList
      ) {
        super(convertMapToParameterDefinitionMap(parameterMap), constraintList);
      }

      private static Map<String, Beans.BaseForFactorSpaceDescriptor.ParameterDefinition> convertMapToParameterDefinitionMap(Map<String, Map<String, Object>> parameterMap) {
        return new HashMap<String, ParameterDefinition>() {{
          parameterMap.keySet()
              .forEach(
                  s -> put(s, new ParameterDefinition(
                      Objects.toString(parameterMap.get(s).get("type")),
                      List.class.<Object>cast(parameterMap.get(s).get("args"))
                  )));
        }};
      }
    }

    public static class TestOracleBean extends Beans.BaseForTestOracle {
      @JsonCreator
      public TestOracleBean(
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
  }
}
