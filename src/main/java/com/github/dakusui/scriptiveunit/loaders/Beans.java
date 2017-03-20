package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.*;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.scriptiveunit.core.Utils.*;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.*;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;

public enum Beans {
  ;
  private static final Object NOP_CLAUSE = Actions.nop();

  public abstract static class BaseForTestSuiteDescriptor {
    final         BaseForCoveringArrayEngineConfig  coveringArrayEngineConfigBean;
    final         BaseForFactorSpaceDescriptor      factorSpaceBean;
    final         List<? extends BaseForTestOracle> testOracleBeanList;
    final         String                            description;
    final         Type                              runnerType;
    private final Map<String, List<Object>>         userDefinedFormClauses;
    private final List<Object>                      setUpClause;
    private final List<Object>                      setUpBeforeAllClause;
    private final List<Object>                      tearDownClause;
    private final List<Object>                      tearDownAfterAllClause;

    public BaseForTestSuiteDescriptor(
        String description, BaseForCoveringArrayEngineConfig coveringArrayEngineConfigBean,
        BaseForFactorSpaceDescriptor factorSpaceBean,
        String runnerType,
        Map<String, List<Object>> userDefinedFormClauses,
        List<Object> setUpBeforeAllClause,
        List<Object> setUpClause,
        List<? extends BaseForTestOracle> testOracleBeanList,
        List<Object> tearDownClause,
        List<Object> tearDownAfterAllClause) {
      this.description = description;
      this.coveringArrayEngineConfigBean = coveringArrayEngineConfigBean;
      this.runnerType = Type.valueOf(Utils.toALL_CAPS(runnerType));
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
          Stage topLevel = session.createTopLevelStage();
          private Statement setUpBeforeAllStatement = topLevel.getStatementFactory().create(setUpBeforeAllClause != null ? setUpBeforeAllClause : NOP_CLAUSE);
          private Statement setUpStatement = topLevel.getStatementFactory().create(setUpClause != null ? setUpClause : NOP_CLAUSE);
          private List<? extends TestOracle> testOracles = createTestOracles();
          private Statement tearDownStatement = topLevel.getStatementFactory().create(tearDownClause != null ? tearDownClause : NOP_CLAUSE);
          private Statement tearDownAfterAllStatement = topLevel.getStatementFactory().create(tearDownAfterAllClause != null ? tearDownAfterAllClause : NOP_CLAUSE);

          List<IndexedTestCase> testCases = createTestCases(this);

          private List<TestOracle> createTestOracles() {
            AtomicInteger i = new AtomicInteger(0);
            return testOracleBeanList.stream().map((BaseForTestOracle each) -> each.create(i.getAndIncrement(), session)).collect(toList());
          }


          @Override
          public String getDescription() {
            return description;
          }

          @Override
          public FactorSpaceDescriptor getFactorSpaceDescriptor() {
            return factorSpaceBean.create(session.getConfig(), new Statement.Factory(session));
          }

          @Override
          public CoveringArrayEngineConfig getCoveringArrayEngineConfig() {
            return coveringArrayEngineConfigBean.create();
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
          public Func<Action> getSetUpBeforeAllActionFactory() {
            return createActionFactory(format("Suite level set up: %s", description), setUpBeforeAllStatement);
          }

          @Override
          public Func<Action> getSetUpActionFactory() {
            return createActionFactory("Fixture set up", setUpStatement);
          }

          @Override
          public Func<Action> getTearDownActionFactory() {
            return createActionFactory("Fixture tear down", tearDownStatement);
          }

          @Override
          public Func<Action> getTearDownAfterAllActionFactory() {
            return createActionFactory(format("Suite level tear down: %s", description), tearDownAfterAllStatement);
          }

          @Override
          public List<String> getInvolvedParameterNamesInSetUpAction() {
            return Statement.Utils.involvedParameters(setUpStatement);
          }

          @Override
          public Config getConfig() {
            return session.getConfig();
          }

          private Func<Action> createActionFactory(String actionName, Statement statement) {
            return (Stage input) -> {
              Object result =
                  statement == null ?
                      nop() :
                      toFunc(statement, new FuncInvoker.Impl(0)).apply(input);
              //noinspection ConstantConditions
              return Actions.named(
                  actionName,
                  Action.class.cast(
                      requireNonNull(
                          result,
                          String.format("statement for '%s' was not valid '%s'", actionName, statement)
                      )));
            };
          }

          @Override
          public Type getRunnerType() {
            return runnerType;
          }

          List<IndexedTestCase> createTestCases(TestSuiteDescriptor testSuiteDescriptor) {
            FactorSpaceDescriptor factorSpaceDescriptor = testSuiteDescriptor.getFactorSpaceDescriptor();
            CoveringArrayEngineConfig coveringArrayEngineConfig = testSuiteDescriptor.getCoveringArrayEngineConfig();

            TestSuite.Builder builder = new TestSuite.Builder(createEngine(coveringArrayEngineConfig));
            builder.disableNegativeTests();
            if (!factorSpaceDescriptor.getFactors().isEmpty()) {
              factorSpaceDescriptor.getFactors().forEach(builder::addFactor);
            } else {
              builder.addFactor("*dummyFactor*", "*dummyLevel*");
            }

            for (TestSuite.Predicate each : factorSpaceDescriptor.getConstraints()) {
              builder.addConstraint(each);
            }
            return builder.build().getTestCases().stream()
                .map(new Function<TestCase, IndexedTestCase>() {
                  int i = 0;

                  @Override
                  public IndexedTestCase apply(TestCase input) {
                    return new IndexedTestCase(i++, input);
                  }
                }).collect(toList());
          }

          CoveringArrayEngine createEngine(CoveringArrayEngineConfig
              coveringArrayEngineConfig) {
            try {
              Constructor<CoveringArrayEngine> constructor;
              return (constructor = Utils.getConstructor(coveringArrayEngineConfig.getEngineClass()))
                  .newInstance(coveringArrayEngineConfig.getOptions().stream()
                      .map(new Function<Object, Object>() {
                        int i = 0;

                        @Override
                        public Object apply(Object input) {
                          try {
                            return convertIfNecessary(input, constructor.getParameterTypes()[i]);
                          } finally {
                            i++;
                          }
                        }
                      })
                      .collect(toList()).toArray());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
              throw wrap(e);
            }
          }
        };
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw wrap(e);
      }
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

    public FactorSpaceDescriptor create(Config config, Statement.Factory statementFactory) {
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
                Func<Boolean> func = toFunc(statement = statementFactory.create(each), new FuncInvoker.Impl(0));
                return new TestSuite.Predicate("(constraint)", Statement.Utils.involvedParameters(statement).toArray(new String[0])) {
                  @Override
                  public boolean apply(Tuple in) {
                    return requireNonNull(func.apply(Stage.Factory.createConstraintGenerationStage(config, statementFactory, in)));
                  }
                };
              })
              .collect(toList());
        }
      };
    }

    private List<Factor> composeFactors(Map<String, List<Object>> factorMap) {
      List<Factor> ret = new LinkedList<>();
      for (String eachFactorName : requireNonNull(factorMap).keySet()) {
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
    private final List<Object> onFailureClause;
    private final List<Object> afterClause;
    private final List<Object> beforeClause;

    public BaseForTestOracle(String description, List<Object> beforeClause, List<Object> givenClause, List<Object> whenClause, List<Object> thenClause, List<Object> onFailureClause, List<Object> afterClause) {
      this.description = description;
      this.beforeClause = beforeClause;
      this.givenClause = givenClause;
      this.whenClause = whenClause;
      this.thenClause = thenClause;
      this.onFailureClause = onFailureClause;
      this.afterClause = afterClause;
    }

    /**
     * Test oracles created by this method are not thread safe since invokers ({@code FuncHandler}
     * objects) have their internal states and not created every time the oracles
     * are performed.
     */
    public TestOracle create(int index, Session session) {
      //noinspection unchecked,Guava
      return new TestOracle() {
        @Override
        public int getIndex() {
          return index;
        }

        @Override
        public String getDescription() {
          return description;
        }

        @Override
        public String templateDescription(Tuple testCaseTuple, String testSuiteDescription) {
          return TestOracle.templateTestOracleDescription(this, testCaseTuple, testSuiteDescription);
        }

        /**
         * Warning: Created action is not thread safe. Users should create 1 action for 1 thread.
         */
        @Override
        public Function<Session, Action> createTestActionFactory(TestItem testItem, Tuple testCaseTuple) {
          int itemId = testItem.getTestItemId();
          Report report = session.createReport(testItem);
          return (Session session) -> sequential(
              format("%03d: %s", itemId, composeDescription(testCaseTuple, session)),
              named("Before", createBefore(testItem, report)),
              attempt(
                  Actions.<Tuple, TestIO>test("Verify with: " + projectMultiLevelFactors(testCaseTuple, session))
                      .given(createGiven(testItem, report, session))
                      .when(createWhen(testItem, report, session))
                      .then(createThen(testItem, report, session)).build()
              ).recover(
                  AssertionError.class,
                  onTestFailure(testItem, report, session)
              ).ensure(
                  named("After", createAfter(testItem, report))
              ).build()
          );
        }

        private Tuple projectMultiLevelFactors(Tuple testCaseTuple, Session session) {
          return Utils.filterSingleLevelFactorsOut(testCaseTuple, session.loadTestSuiteDescriptor().getFactorSpaceDescriptor().getFactors());
        }

        private String composeDescription(Tuple testCaseTuple, Session session) {
          return template(description, append(testCaseTuple, "@TESTSUITE", session.loadTestSuiteDescriptor().getDescription()));
        }


        private Action createBefore(TestItem testItem, Report report) {
          return createActionFromClause(BEFORE, beforeClause, testItem, report);
        }

        private Source<Tuple> createGiven(final TestItem testItem, final Report report, final Session session) {
          Tuple testCaseTuple = testItem.getTestCaseTuple();
          return new Source<Tuple>() {
            FuncInvoker funcInvoker = new FuncInvoker.Impl(0);
            Stage givenStage = Stage.Factory.createOracleLevelStage(GIVEN, session, testItem, report);
            Statement givenStatement = givenStage.getStatementFactory().create(givenClause);

            @Override
            public Tuple apply(Context context) {
              assumeThat(testCaseTuple, new BaseMatcher<Tuple>() {
                @Override
                public boolean matches(Object item) {
                  return requireNonNull(Beans.<Boolean>toFunc(givenStatement, funcInvoker).apply(givenStage));
                }

                @Override
                public void describeTo(Description description) {
                  description.appendText(
                      format("input (%s) should have made true following criterion but not.:%n'%s' defined in stage:%s",
                          testCaseTuple,
                          funcInvoker.asString(),
                          GIVEN));
                }
              });
              return testCaseTuple;
            }

            @Override
            public String toString() {
              return format("%n%s", funcInvoker.asString());
            }
          };
        }

        private Pipe<Tuple, TestIO> createWhen(final TestItem testItem, final Report report, final Session session) {
          return new Pipe<Tuple, TestIO>() {
            FuncInvoker funcInvoker = new FuncInvoker.Impl(0);

            @Override
            public TestIO apply(Tuple testCase, Context context) {
              Stage whenStage = Stage.Factory.createOracleLevelStage(WHEN, session, testItem, report);
              return TestIO.create(
                  testCase,
                  Beans.<Boolean>toFunc(
                      whenStage.getStatementFactory().create(whenClause),
                      funcInvoker
                  ).apply(whenStage));
            }

            @Override
            public String toString() {
              return format("%n%s", funcInvoker.asString());
            }
          };
        }

        private Sink<TestIO> createThen(TestItem testItem, final Report report, final Session session) {
          return new Sink<TestIO>() {
            FuncInvoker funcInvoker = new FuncInvoker.Impl(0);

            @Override
            public void apply(TestIO testIO, Context context) {
              Stage thenStage = Stage.Factory.createOracleVerificationStage(session, testItem, testIO.getOutput(), report);
              assertThat(
                  thenStage,
                  new BaseMatcher<Stage>() {
                    @Override
                    public boolean matches(Object item) {
                      return requireNonNull(
                          Beans.<Boolean>toFunc(
                              thenStage.getStatementFactory().create(thenClause),
                              funcInvoker
                          ).apply(thenStage));
                    }

                    @Override
                    public void describeTo(Description description) {
                      description.appendText(format("output should have made true the criterion defined in stage:%s", thenStage.getType()));
                    }

                    @Override
                    public void describeMismatch(Object item, Description description) {
                      Object output = testIO.getOutput() instanceof Iterable ?
                          iterableToString((Iterable<?>) testIO.getOutput()) :
                          testIO.getOutput();
                      description.appendText(String.format("output '%s' created from '%s' did not satisfy it.:%n'%s'",
                          output,
                          testItem.getTestCaseTuple(),
                          funcInvoker.asString()));
                    }
                  }
              );
            }

            @Override
            public String toString() {
              return format("%n%s", funcInvoker.asString());
            }
          };
        }

        private <T extends AssertionError> Sink<T> onTestFailure(TestItem testItem, Report report, Session session) {
          return new Sink<T>() {
            FuncInvoker funcInvoker = new FuncInvoker.Impl(0);

            @Override
            public void apply(T input, Context context) {
              Stage onFailureStage = Stage.Factory.createOracleFailureHandlingStage(session, testItem, input, report);
              Statement onFailureStatement = onFailureStage.getStatementFactory().create(onFailureClause);
              Utils.performActionWithLogging(requireNonNull(
                  onFailureClause != null ?
                      Beans.<Action>toFunc(onFailureStatement, funcInvoker) :
                      (Func<Action>) input1 -> Actions.nop()).apply(onFailureStage));
              throw requireNonNull(input);
            }

            @Override
            public String toString() {
              return format("%n%s", funcInvoker.asString());
            }
          };
        }

        private Action createAfter(TestItem testItem, Report report) {
          return createActionFromClause(AFTER, afterClause, testItem, report);
        }

        private Action createActionFromClause(Stage.Type stageType, List<Object> clause, final TestItem testItem, Report report) {
          if (clause == null)
            return (Action) NOP_CLAUSE;
          Stage stage = Stage.Factory.createOracleLevelStage(stageType, session, testItem, report);
          Statement statement = stage.getStatementFactory().create(clause);
          FuncInvoker funcInvoker = new FuncInvoker.Impl(0);
          return Beans.<Action>toFunc(statement, funcInvoker).apply(stage);
        }
      };
    }
  }

  private static <U> Func<U> toFunc(Statement statement, FuncInvoker funcInvoker) {
    //noinspection unchecked
    return Func.class.<U>cast(statement.compile(funcInvoker));
  }
}
