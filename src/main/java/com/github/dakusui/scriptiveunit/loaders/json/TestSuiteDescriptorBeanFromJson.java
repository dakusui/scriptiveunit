package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.loaders.beans.FactorSpaceDescriptorBean;
import com.github.dakusui.scriptiveunit.loaders.beans.TestSuiteDescriptorBean;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TestSuiteDescriptorBeanFromJson extends TestSuiteDescriptorBean {
  public TestSuiteDescriptorBeanFromJson(
      @JsonProperty("description") String description,
      @JsonProperty("factorSpace") FactorSpaceDescriptorBeanFromJson factorSpaceBean,
      @JsonProperty("runnerType") String runnerType,
      @JsonProperty("define") Map<String, List<Object>> userFormClauseMap,
      @JsonProperty("setUpBeforeAll") List<Object> setUpBeforeAllClause,
      @JsonProperty("setUp") List<Object> setUpClause,
      @JsonProperty("testOracles") List<JsonTestOracleBean> testOracleBeanList,
      @JsonProperty("tearDown") List<Object> tearDownClause,
      @JsonProperty("tearDownAfterAll") List<Object> tearDownAfterAllClause
  ) {
    super(description,
        factorSpaceBean,
        runnerType,
        userFormClauseMap,
        setUpBeforeAllClause,
        setUpClause,
        testOracleBeanList,
        tearDownClause,
        tearDownAfterAllClause);
  }

  public static class FactorSpaceDescriptorBeanFromJson extends FactorSpaceDescriptorBean {
    public FactorSpaceDescriptorBeanFromJson(
        @JsonProperty("factors") Map<String, Map<String, Object>> parameterMap,
        @JsonProperty("constraints") List<List<Object>> constraintList
    ) {
      super(convertMapToParameterDefinitionMap(parameterMap), constraintList);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ParameterDefinition> convertMapToParameterDefinitionMap(Map<String, Map<String, Object>> parameterMap) {
      return new HashMap<String, ParameterDefinition>() {{
        parameterMap.keySet()
            .forEach(
                s -> put(s, new ParameterDefinition(
                    Objects.toString(parameterMap.get(s).get("type")),
                    (List) parameterMap.get(s).get("args")
                )));
      }};
    }
  }

}
