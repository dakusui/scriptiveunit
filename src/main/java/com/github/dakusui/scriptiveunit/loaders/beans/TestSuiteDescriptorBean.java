package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.ParameterSpaceDescriptor;
import com.github.dakusui.scriptiveunit.model.session.Session;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormInvoker;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.github.dakusui.actionunit.Actions.nop;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.model.form.FormInvoker.createMemo;
import static com.github.dakusui.scriptiveunit.model.statement.Statement.createStatementFactory;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public abstract class TestSuiteDescriptorBean {
  private final FactorSpaceDescriptorBean      factorSpaceBean;
  private final List<? extends TestOracleBean> testOracleBeanList;
  private final String                         description;
  private final ScriptiveUnit.Mode             runnerMode;
  private final Map<String, List<Object>>      userDefinedFormClauses;
  private final List<Object>                   setUpClause;
  private final List<Object>                   setUpBeforeAllClause;
  private final List<Object>                   tearDownClause;
  private final List<Object>                   tearDownAfterAllClause;

  public TestSuiteDescriptorBean(
      String description,
      FactorSpaceDescriptorBean factorSpaceBean,
      String runnerType,
      Map<String, List<Object>> userDefinedFormClauses,
      List<Object> setUpBeforeAllClause,
      List<Object> setUpClause,
      List<? extends TestOracleBean> testOracleBeanList,
      List<Object> tearDownClause,
      List<Object> tearDownAfterAllClause) {
    this.description = description;
    this.runnerMode = ScriptiveUnit.Mode.valueOf(Utils.toALL_CAPS(runnerType));
    this.factorSpaceBean = factorSpaceBean;
    this.userDefinedFormClauses = userDefinedFormClauses;
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
        private Statement setUpBeforeAllStatement =
            statementFactory.create(setUpBeforeAllClause != null ? setUpBeforeAllClause : BeanUtils.NOP_CLAUSE);
        private Statement setUpStatement =
            statementFactory.create(setUpClause != null ? setUpClause : BeanUtils.NOP_CLAUSE);
        private List<? extends TestOracle> testOracles = createTestOracles();
        private Statement tearDownStatement = statementFactory.create(tearDownClause != null ? tearDownClause : BeanUtils.NOP_CLAUSE);
        private Statement tearDownAfterAllStatement = statementFactory.create(tearDownAfterAllClause != null ? tearDownAfterAllClause : BeanUtils.NOP_CLAUSE);

        List<IndexedTestCase> testCases = createTestCases(this);

        private List<TestOracle> createTestOracles() {
          AtomicInteger i = new AtomicInteger(0);
          return testOracleBeanList.stream().map((TestOracleBean each) -> each.create(i.getAndIncrement(), this)).collect(toList());
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
        public Map<String, List<Object>> getUserDefinedFormClauses() {
          return userDefinedFormClauses;
        }

        @Override
        public Form<Action> getSetUpBeforeAllActionFactory() {
          return createActionFactory(
              format("Suite level set up: %s", description),
              setUpBeforeAllStatement
          );
        }

        @Override
        public Form<Action> getSetUpActionFactory() {
          return createActionFactory("Fixture set up", setUpStatement);
        }

        @Override
        public Form<Action> getTearDownActionFactory() {
          return createActionFactory("Fixture tear down", tearDownStatement);
        }

        @Override
        public Form<Action> getTearDownAfterAllActionFactory() {
          return createActionFactory(
              format("Suite level tear down: %s", description),
              tearDownAfterAllStatement
          );
        }

        @Override
        public List<String> getInvolvedParameterNamesInSetUpAction() {
          return Statement.Utils.involvedParameters(setUpStatement);
        }

        @Override
        public Config getConfig() {
          return session.getConfig();
        }

        @Override
        public Statement.Factory statementFactory() {
          return statementFactory;
        }

        private Form<Action> createActionFactory(String actionName, Statement statement) {
          return (Stage input) -> {
            Object result =
                statement == null ?
                    nop() :
                    BeanUtils.toForm(statement, FormInvoker.create(createMemo())).apply(input);
            return (Action) requireNonNull(
                result,
                String.format("statement for '%s' was not valid '%s'", actionName, statement)
            );
          };
        }

        @Override
        public ScriptiveUnit.Mode getRunnerType() {
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
      throw wrap(e);
    }
  }

}
