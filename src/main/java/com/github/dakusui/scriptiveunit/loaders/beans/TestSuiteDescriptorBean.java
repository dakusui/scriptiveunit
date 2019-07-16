package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.model.desc.ParameterSpaceDescriptor;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.value.ValueUtils;
import com.github.dakusui.scriptiveunit.model.session.Session;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.runners.RunningMode;
import com.github.dakusui.scriptiveunit.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.model.statement.Statement.createStatementFactory;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public abstract class TestSuiteDescriptorBean {
  private final FactorSpaceDescriptorBean      factorSpaceBean;
  private final List<? extends TestOracleBean> testOracleBeanList;
  private final String                         description;
  private final RunningMode                    runnerMode;
  private final Map<String, List<Object>>      userDefinedFormClauseMap;
  private final List<Object>                   setUpClause;
  private final List<Object>                   setUpBeforeAllClause;
  private final List<Object>                   tearDownClause;
  private final List<Object>                   tearDownAfterAllClause;

  public TestSuiteDescriptorBean(
      String description,
      FactorSpaceDescriptorBean factorSpaceBean,
      String runnerType,
      Map<String, List<Object>> userDefinedFormClauseMap,
      List<Object> setUpBeforeAllClause,
      List<Object> setUpClause,
      List<? extends TestOracleBean> testOracleBeanList,
      List<Object> tearDownClause,
      List<Object> tearDownAfterAllClause) {
    this.description = description;
    this.runnerMode = RunningMode.valueOf(StringUtils.toALL_CAPS(runnerType));
    this.factorSpaceBean = factorSpaceBean;
    this.userDefinedFormClauseMap = userDefinedFormClauseMap;
    this.setUpBeforeAllClause = setUpBeforeAllClause;
    this.setUpClause = setUpClause;
    this.testOracleBeanList = testOracleBeanList;
    this.tearDownClause = tearDownClause;
    this.tearDownAfterAllClause = tearDownAfterAllClause;
  }

  public TestSuiteDescriptor create(Session session) {
    try {
      return new TestSuiteDescriptor() {
        private final Statement.Factory statementFactory =
            createStatementFactory(session.getConfig(), this.getUserDefinedFormClauses());
        private Statement setUpBeforeAllStatement = setUpBeforeAllClause != null ?
            statementFactory.create(setUpBeforeAllClause) :
            null;
        private Statement setUpStatement = setUpClause != null ?
            statementFactory.create(setUpClause) :
            null;
        private List<? extends TestOracle> testOracles = createTestOracles();
        private Statement tearDownStatement = tearDownClause != null ?
            statementFactory.create(tearDownClause) :
            null;
        private Statement tearDownAfterAllStatement = tearDownAfterAllClause != null ?
            statementFactory.create(tearDownAfterAllClause) :
            null;

        List<IndexedTestCase> testCases = createTestCases(this);

        private List<TestOracle> createTestOracles() {
          AtomicInteger i = new AtomicInteger(0);
          return testOracleBeanList
              .stream()
              .map((TestOracleBean each) -> each.createTestOracle(i.getAndIncrement(), this))
              .collect(toList());
        }

        @Override
        public String getDescription() {
          return description;
        }

        @Override
        public ParameterSpaceDescriptor getFactorSpaceDescriptor() {
          return factorSpaceBean.create(session, statementFactory);
        }

        @Override
        public List<? extends TestOracle> getTestOracles() {
          return this.testOracles;
        }

        @Override
        public List<IndexedTestCase> getTestCases() {
          return this.testCases;
        }

        @Override
        public Optional<Statement> setUpBeforeAll() {
          return Optional.ofNullable(setUpBeforeAllStatement);
        }

        @Override
        public Optional<Statement> setUp() {
          return Optional.ofNullable(setUpStatement);
        }

        @Override
        public Optional<Statement> tearDown() {
          return Optional.ofNullable(tearDownStatement);
        }

        @Override
        public Optional<Statement> tearDownAfterAll() {
          return Optional.ofNullable(tearDownAfterAllStatement);
        }

        @Override
        public Map<String, List<Object>> getUserDefinedFormClauses() {
          return userDefinedFormClauseMap;
        }

        @Override
        public List<String> getInvolvedParameterNamesInSetUpAction() {
          return setUp()
              .map(ValueUtils::involvedParameters)
              .orElse(emptyList());
        }

        @Override
        public Config getConfig() {
          return session.getConfig();
        }

        @Override
        public Statement.Factory statementFactory() {
          return statementFactory;
        }

        @Override
        public RunningMode getRunnerMode() {
          return runnerMode;
        }

        private List<IndexedTestCase> createTestCases(TestSuiteDescriptor testSuiteDescriptor) {
          ParameterSpace parameterSpace = createParameterSpaceFrom(testSuiteDescriptor);
          if (parameterSpace.getParameterNames().isEmpty())
            return singletonList(new IndexedTestCase(0, new Tuple.Builder().build(), TestCase.Category.REGULAR));
          return Pipeline.Standard.create().execute(
              new com.github.dakusui.jcunit8.pipeline.Config.Builder(
                  new Requirement.Builder().withNegativeTestGeneration(false).withStrength(2).build()
              ).build(),
              parameterSpace
          ).stream()
              .map(new Function<TestCase, IndexedTestCase>() {
                int i = 0;

                @Override
                public IndexedTestCase apply(TestCase testCase) {
                  return new IndexedTestCase(i++, testCase);
                }
              })
              .collect(toList());
        }

        private ParameterSpace createParameterSpaceFrom(TestSuiteDescriptor testSuiteDescriptor) {
          return new ParameterSpace.Builder()
              .addAllParameters(
                  testSuiteDescriptor.getFactorSpaceDescriptor().getParameters()
              )
              .addAllConstraints(
                  testSuiteDescriptor.getFactorSpaceDescriptor().getConstraints()
              )
              .build();
        }
      };
    } catch (Exception e) {
      throw ScriptiveUnitException.wrapIfNecessary(e);
    }
  }
}
