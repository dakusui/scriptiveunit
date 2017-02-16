package com.github.dakusui.scriptunit.loaders;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.scriptunit.ScriptRunner.Type;
import com.github.dakusui.scriptunit.core.Utils;
import com.github.dakusui.scriptunit.model.*;
import com.github.dakusui.scriptunit.model.func.Func;
import com.github.dakusui.scriptunit.model.func.FuncInvoker;
import com.github.dakusui.scriptunit.model.statement.Statement;
import com.google.common.collect.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.dakusui.actionunit.Actions.named;
import static com.github.dakusui.scriptunit.core.Utils.iterableToString;
import static com.github.dakusui.scriptunit.exceptions.ScriptUnitException.wrap;
import static com.github.dakusui.scriptunit.model.Stage.Type.*;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;

public enum Beans {
  ;

  public abstract static class BaseForTestSuiteDescriptor {
    final         BaseForCoveringArrayEngineConfig  coveringArrayEngineConfigBean;
    final         BaseForFactorSpaceDescriptor      factorSpaceBean;
    final         List<? extends BaseForTestOracle> testOracleBeanList;
    final         String                            description;
    final         Type                              runnerType;
    private final List<Object>                      setUpClause;
    private final List<Object>                      setUpBeforeAllClause;

    public BaseForTestSuiteDescriptor(
        BaseForCoveringArrayEngineConfig coveringArrayEngineConfigBean,
        BaseForFactorSpaceDescriptor factorSpaceBean,
        List<Object> suiteLevelFixture,
        List<Object> fixture,
        List<? extends BaseForTestOracle> testOracleBeanList, String description, String runnerType) {
      this.coveringArrayEngineConfigBean = coveringArrayEngineConfigBean;
      this.runnerType = Type.valueOf(Utils.toALL_CAPS(runnerType));
      this.factorSpaceBean = factorSpaceBean;
      this.setUpBeforeAllClause = suiteLevelFixture;
      this.setUpClause = fixture;
      this.testOracleBeanList = testOracleBeanList;
      this.description = description;
    }


    public TestSuiteDescriptor create(Object driverObject) {
      return new TestSuiteDescriptor() {
        private final Object NOP_CLAUSE = Lists.newArrayList("nop");
        Statement setUpStatement = new Statement.Factory(driverObject).create(setUpClause != null ? setUpClause : NOP_CLAUSE);
        Statement setUpBeforeAllStatement = new Statement.Factory(driverObject).create(setUpBeforeAllClause != null ? setUpBeforeAllClause : NOP_CLAUSE);

        @Override
        public String getDescription() {
          return description;
        }

        @Override
        public FactorSpaceDescriptor getFactorSpaceDescriptor() {
          return factorSpaceBean.create(new Statement.Factory(driverObject));
        }

        @Override
        public CoveringArrayEngineConfig getCoveringArrayEngineConfig() {
          return coveringArrayEngineConfigBean.create();
        }

        @Override
        public List<? extends TestOracle> getTestOracles() {
          return testOracleBeanList.stream().map(each -> each.create(new Statement.Factory(driverObject))).collect(Collectors.toList());
        }

        @Override
        public Func<Stage, Action> getSetUpActionFactory() {
          return createActionFactory("Fixture set up", setUpStatement);
        }

        @Override
        public List<String> getInvolvedParameterNamesInSetUpAction() {
          return Statement.Utils.involvedParameters(setUpStatement);
        }

        @Override
        public Func<Stage, Action> getSetUpBeforeAllActionFactory() {
          return createActionFactory(format("Suite level set up: %s", description), setUpBeforeAllStatement);
        }


        private Func<Stage, Action> createActionFactory(String actionName, Statement statement) {
          return input -> Actions.named(
              actionName,
              statement == null ?
                  Actions.nop() :
                  requireNonNull(Beans.<Stage, Action>toFunc(statement, new FuncInvoker.Impl(0)).apply(input))
          );
        }

        @Override
        public Type getRunnerType() {
          return runnerType;
        }
      };
    }

  }

  public abstract static class BaseForCoveringArrayEngineConfig {
    private final Class<? extends CoveringArrayEngine> coveringArrayEngineClass;
    private final List<Object>                         options;

    public BaseForCoveringArrayEngineConfig(String className, List<Object> options) {
      try {
        Class clazz = Class.class.cast(Class.forName(className));
        checkArgument(CoveringArrayEngine.class.isAssignableFrom(clazz));
        //noinspection unchecked
        this.coveringArrayEngineClass = clazz;
        this.options = options;
      } catch (ClassNotFoundException e) {
        throw wrap(e);
      }
    }

    public CoveringArrayEngineConfig create() {
      return new CoveringArrayEngineConfig() {
        @Override
        public Class<? extends CoveringArrayEngine> getEngineClass() {
          return coveringArrayEngineClass;
        }

        @Override
        public List<Object> getOptions() {
          return options;
        }
      };
    }
  }

  /**
   * A base class for factor space descriptors.
   */
  public abstract static class BaseForFactorSpaceDescriptor {
    private final Map<String, List<Object>> factorMap;
    private final List<List<Object>>        constraintList;

    public BaseForFactorSpaceDescriptor(Map<String, List<Object>> factorMap, List<List<Object>> constraintList) {
      this.factorMap = factorMap;
      this.constraintList = constraintList;
    }

    public FactorSpaceDescriptor create(Statement.Factory statementFactory) {
      return new FactorSpaceDescriptor() {
        @Override
        public List<Factor> getFactors() {
          return composeFactors(BaseForFactorSpaceDescriptor.this.factorMap);
        }

        @Override
        public List<TestSuite.Predicate> getConstraints() {
          //noinspection unchecked
          return constraintList.stream()
              .map((List<Object> each) -> {
                //noinspection unchecked
                Statement statement;
                Func<Stage, Boolean> func = toFunc(statement = statementFactory.create(each), new FuncInvoker.Impl(0));
                return new TestSuite.Predicate("(constraint)", Statement.Utils.involvedParameters(statement).toArray(new String[0])) {
                  @Override
                  public boolean apply(Tuple in) {
                    return requireNonNull(func.apply(new Stage() {
                      @Override
                      public Tuple getTestCaseTuple() {
                        return in;
                      }

                      @Override
                      public <RESPONSE> RESPONSE response() {
                        throw new UnsupportedOperationException();
                      }

                      @Override
                      public Type getType() {
                        return GIVEN;
                      }
                    }));
                  }
                };
              })
              .collect(Collectors.toList());
        }
      };
    }

    private List<Factor> composeFactors(Map<String, List<Object>> factorMap) {
      List<Factor> ret = new LinkedList<>();
      for (String eachFactorName : factorMap.keySet()) {
        Factor.Builder b = new Factor.Builder(eachFactorName);
        for (Object eachLevel : factorMap.get(eachFactorName)) {
          b.addLevel(eachLevel);
        }
        ret.add(b.build());
      }
      return ret;
    }

  }

  public abstract static class BaseForTestOracle {
    private final String       description;
    private final List<Object> givenClause;
    private final List<Object> whenClause;
    private final List<Object> thenClause;

    public BaseForTestOracle(String description, List<Object> givenClause, List<Object> whenClause, List<Object> thenClause) {
      this.description = description;
      this.givenClause = givenClause;
      this.whenClause = whenClause;
      this.thenClause = thenClause;
    }

    /**
     * Test oracles created by this method are not thread safe since invokers ({@code FuncHandler}
     * objects) have their internal states and not created every time the oracles
     * are performed.
     *
     * @param statementFactory A factory that creates {@code Statement} objects.
     */
    public TestOracle create(Statement.Factory statementFactory) {
      //noinspection unchecked,Guava
      return new TestOracle() {
        @Override
        public String getDescription() {
          return description;
        }

        /**
         * Warning: Created action is not thread safe. Users should create 1 action for 1 thread.
         */
        @Override
        public Supplier<Action> createTestActionSupplier(List<Factor> factors, int itemId, String testSuiteDescription, Tuple testCaseTuple) {
          return () -> named(format("%03d: %s", itemId, description),
              Actions.<Tuple, TestResult>test("verify with: " + Utils.filterSingleLevelFactorsOut(testCaseTuple, factors))
                  .given(new Source<Tuple>() {
                    Statement givenStatement = statementFactory.create(givenClause);
                    FuncInvoker funcInvoker = new FuncInvoker.Impl(0);

                    @Override
                    public Tuple apply(Context context) {
                      assumeThat(Statement.Utils.prettifyTuple(testCaseTuple, givenStatement), new BaseMatcher<Tuple>() {
                        @Override
                        public boolean matches(Object item) {
                          return requireNonNull(
                              createFunc(givenStatement, funcInvoker).apply(GIVEN.create(testCaseTuple))
                          );
                        }

                        @Override
                        public void describeTo(Description description) {
                          description.appendText(
                              format("input (%s) should have made true following criterion but not.:%n'%s' defined in stage:%s",
                                  testCaseTuple,
                                  funcInvoker.asString(),
                                  GIVEN));
                        }

                        private Func<Stage, Boolean> createFunc(Statement statement, FuncInvoker invoker) {
                          return Beans.toFunc(statement, invoker);
                        }
                      });
                      return testCaseTuple;
                    }

                    @Override
                    public String toString() {
                      return format("%n%s", funcInvoker.asString());
                    }
                  })
                  .when(new Pipe<Tuple, TestResult>() {
                    FuncInvoker funcInvoker = new FuncInvoker.Impl(0);

                    @Override
                    public TestResult apply(Tuple testCase, Context context) {
                      return TestResult.create(
                          testCase,
                          Beans.<Stage, Boolean>toFunc(statementFactory.create(whenClause), funcInvoker)
                              .apply(WHEN.create(testCase)));
                    }

                    @Override
                    public String toString() {
                      return format("%n%s", funcInvoker.asString());
                    }
                  })
                  .then(new Sink<TestResult>() {
                    FuncInvoker funcInvoker = new FuncInvoker.Impl(0);

                    @Override
                    public void apply(TestResult testResult, Context context) {
                      Stage thenStage = THEN.create(testResult.getTestCase(), testResult.getOutput());
                      assertThat(
                          thenStage,
                          new BaseMatcher<Stage>() {
                            @Override
                            public boolean matches(Object item) {
                              return requireNonNull(
                                  Beans.<Stage, Boolean>toFunc(statementFactory.create(thenClause), funcInvoker)
                                      .apply(thenStage)
                              );
                            }

                            @Override
                            public void describeTo(Description description) {
                              description.appendText(format("output should have made true the criterion defined in stage:%s", thenStage.getType()));
                            }

                            @Override
                            public void describeMismatch(Object item, Description description) {
                              Object output = testResult.getOutput() instanceof Iterable ?
                                  iterableToString((Iterable<?>) testResult.getOutput()) :
                                  testResult.getOutput();
                              description.appendText(format("output '%s' created from '%s' did not satisfy it.:%n'%s'",
                                  output,
                                  testResult.getTestCase(),
                                  funcInvoker.asString()));
                            }
                          }
                      );
                    }

                    @Override
                    public String toString() {
                      return format("%n%s", funcInvoker.asString());
                    }
                  }).build()
          );
        }
      };
    }
  }

  private static <T extends Stage, U> Func<T, U> toFunc(Statement statement, FuncInvoker funcInvoker) {
    //noinspection unchecked
    return Func.class.<T, U>cast(statement.executeWith(funcInvoker));
  }
}
