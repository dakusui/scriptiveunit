package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.spec.FsmSpec;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.model.*;
import com.github.dakusui.scriptiveunit.model.func.Func;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.scriptiveunit.core.Utils.*;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.*;
import static java.lang.Class.forName;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;

public enum Beans {
  ;
  private static final Object NOP_CLAUSE = Actions.nop();

  public abstract static class BaseForTestSuiteDescriptor {
    final         BaseForFactorSpaceDescriptor                                   factorSpaceBean;
    final         List<? extends BaseForTestOracle> testOracleBeanList;
    final         String                                                         description;
    final         Type                                                           runnerType;
    private final Map<String, List<Object>>                                      userDefinedFormClauses;
    private final List<Object>                                                   setUpClause;
    private final List<Object>                                                   setUpBeforeAllClause;
    private final List<Object>                                                   tearDownClause;
    private final List<Object>                                                   tearDownAfterAllClause;

    public BaseForTestSuiteDescriptor(
        String description,
        BaseForFactorSpaceDescriptor factorSpaceBean,
        String runnerType,
        Map<String, List<Object>> userDefinedFormClauses,
        List<Object> setUpBeforeAllClause,
        List<Object> setUpClause,
        List<? extends BaseForTestOracle> testOracleBeanList,
        List<Object> tearDownClause,
        List<Object> tearDownAfterAllClause) {
      this.description = description;
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
          public ParameterSpaceDescriptor getFactorSpaceDescriptor() {
            return factorSpaceBean.create(session.getConfig(), new Statement.Factory(session));
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
                      toFunc(statement, FuncInvoker.create()).apply(input);
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
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw wrap(e);
      }
    }

  }

  /**
   * A base class for factor space descriptors.
   */
  public abstract static class BaseForFactorSpaceDescriptor {
    protected static class ParameterDefinition {
      String       type;
      List<Object> args;

      public ParameterDefinition(String type, List<Object> args) {
        this.type = type;
        this.args = args;
      }
    }

    private final Map<String, ParameterDefinition> parameterDefinitionMap;
    private final List<List<Object>>               constraintList;

    public BaseForFactorSpaceDescriptor(Map<String, ParameterDefinition> parameterDefinitionMap, List<List<Object>> constraintList) {
      this.parameterDefinitionMap = parameterDefinitionMap;
      this.constraintList = constraintList;
    }

    public ParameterSpaceDescriptor create(Config config, Statement.Factory statementFactory) {
      return new ParameterSpaceDescriptor() {
        @Override
        public List<Parameter> getParameters() {
          return composeParameters(BaseForFactorSpaceDescriptor.this.parameterDefinitionMap);
        }

        @Override
        public List<Constraint> getConstraints() {
          //noinspection unchecked
          return constraintList.stream()
              .map((List<Object> each) -> {
                //noinspection unchecked
                Statement statement = statementFactory.create(each);
                Func<Boolean> func = toFunc(
                    statement,
                    FuncInvoker.create()
                );
                return Constraint.create(
                    in -> requireNonNull(func.apply(Stage.Factory.createConstraintGenerationStage(config, statementFactory, in))),
                    Statement.Utils.involvedParameters(statement)
                );
              })
              .collect(toList());
        }
      };
    }

    private List<Parameter> composeParameters(Map<String, ParameterDefinition> factorMap) {
      return requireNonNull(factorMap).keySet().stream()
          .map(
              (String parameterName) -> {
                ParameterDefinition def = requireNonNull(factorMap.get(parameterName));
                switch (def.type) {
                case "simple":
                  return Parameter.Simple.Factory.of(validateParameterDefinitionArgsForSimple(def.args)).create(parameterName);
                case "regex":
                  return Parameter.Regex.Factory.of(Objects.toString(validateParameterDefinitionArgsForRegex(def.args).get(0))).create(parameterName);
                case "fsm":
                  try {
                    validateParameterDefinitionArgsForFsm(def.args);
                    return Parameter.Fsm.Factory.<Object>of(
                        (Class<? extends FsmSpec<Object>>) forName(Objects.toString(def.args.get(0))),
                        Integer.valueOf(Objects.toString(def.args.get(1))).intValue()
                    ).create(parameterName);
                  } catch (ClassCastException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                  }
                default:
                  throw new RuntimeException(
                      String.format(
                          "unknown type '%s' was given to parameter '%s''s definition.",
                          def.type,
                          parameterName
                      ));
                }
              })
          .collect(Collectors.toList());
    }

    private List<Object> validateParameterDefinitionArgsForSimple(List<Object> def) {
      return def;
    }

    private List<Object> validateParameterDefinitionArgsForRegex(List<Object> def) {
      if (def.size() != 1)
        throw ScriptiveUnitException.fail("").get();
      return def;
    }

    private List<Object> validateParameterDefinitionArgsForFsm(List<Object> def) {
      return def;
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
        public Function<Session, Action> createTestActionFactory(TestItem testItem, Tuple testCaseTuple, Map<List<Object>, Object> memo) {
          int itemId = testItem.getTestItemId();
          Report report = session.createReport(testItem);
          return (Session session) -> sequential(
              format("%03d: %s", itemId, composeDescription(testCaseTuple, session)),
              named("Before", createBefore(testItem, report, memo)),
              attempt(
                  Actions.<Tuple, TestIO>test("Verify with: " + projectMultiLevelFactors(testCaseTuple, session))
                      .given(createGiven(testItem, report, session, memo))
                      .when(createWhen(testItem, report, session, memo))
                      .then(createThen(testItem, report, session, memo)).build()
              ).recover(
                  AssertionError.class,
                  onTestFailure(testItem, report, session, memo)
              ).ensure(
                  named("After", createAfter(testItem, report, memo))
              ).build()
          );
        }

        private Tuple projectMultiLevelFactors(Tuple testCaseTuple, Session session) {
          return Utils.filterSimpleSingleLevelParametersOut(
              testCaseTuple,
              session.loadTestSuiteDescriptor().getFactorSpaceDescriptor().getParameters()
          );
        }

        private String composeDescription(Tuple testCaseTuple, Session session) {
          return template(description, append(testCaseTuple, "@TESTSUITE", session.loadTestSuiteDescriptor().getDescription()));
        }


        private Action createBefore(TestItem testItem, Report report, Map<List<Object>, Object> memo) {
          return createActionFromClause(BEFORE, beforeClause, testItem, report, memo);
        }

        private Source<Tuple> createGiven(final TestItem testItem, final Report report, final Session session, Map<List<Object>, Object> memo) {
          Tuple testCaseTuple = testItem.getTestCaseTuple();
          return new Source<Tuple>() {
            FuncInvoker funcInvoker = FuncInvoker.create(memo);
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

        private Pipe<Tuple, TestIO> createWhen(final TestItem testItem, final Report report, final Session session, Map<List<Object>, Object> memo) {
          return new Pipe<Tuple, TestIO>() {
            FuncInvoker funcInvoker = FuncInvoker.create(memo);

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

        private Sink<TestIO> createThen(TestItem testItem, final Report report, final Session session, Map<List<Object>, Object> memo) {
          return new Sink<TestIO>() {
            FuncInvoker funcInvoker = FuncInvoker.create(memo);

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

        private <T extends AssertionError> Sink<T> onTestFailure(TestItem testItem, Report report, Session session, Map<List<Object>, Object> memo) {
          return new Sink<T>() {
            FuncInvoker funcInvoker = FuncInvoker.create(memo);

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

        private Action createAfter(TestItem testItem, Report report, Map<List<Object>, Object> memo) {
          return createActionFromClause(AFTER, afterClause, testItem, report, memo);
        }

        private Action createActionFromClause(Stage.Type stageType, List<Object> clause, final TestItem testItem, Report report, Map<List<Object>, Object> memo) {
          if (clause == null)
            return (Action) NOP_CLAUSE;
          Stage stage = Stage.Factory.createOracleLevelStage(stageType, session, testItem, report);
          Statement statement = stage.getStatementFactory().create(clause);
          FuncInvoker funcInvoker = FuncInvoker.create(memo);
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
