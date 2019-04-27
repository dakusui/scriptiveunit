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
import com.github.dakusui.scriptiveunit.model.Session;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.model.ParameterSpaceDescriptor;
import com.github.dakusui.scriptiveunit.model.Report;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestIO;
import com.github.dakusui.scriptiveunit.model.TestItem;
import com.github.dakusui.scriptiveunit.model.TestOracle;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.func.Form;
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

import static com.github.dakusui.actionunit.Actions.attempt;
import static com.github.dakusui.actionunit.Actions.nop;
import static com.github.dakusui.actionunit.Actions.sequential;
import static com.github.dakusui.scriptiveunit.core.Utils.append;
import static com.github.dakusui.scriptiveunit.core.Utils.iterableToString;
import static com.github.dakusui.scriptiveunit.core.Utils.template;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.AFTER;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.BEFORE;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.GIVEN;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.WHEN;
import static com.github.dakusui.scriptiveunit.model.func.FuncInvoker.createMemo;
import static com.github.dakusui.scriptiveunit.model.statement.Statement.createStatementFactory;
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
          private final Statement.Factory statementFactory =
              createStatementFactory(session.getConfig(), this.getUserDefinedFormClauses());
          private Statement setUpBeforeAllStatement =
              statementFactory.create(setUpBeforeAllClause != null ? setUpBeforeAllClause : NOP_CLAUSE);
          private Statement setUpStatement =
              statementFactory.create(setUpClause != null ? setUpClause : NOP_CLAUSE);
          private List<? extends TestOracle> testOracles = createTestOracles();
          private Statement tearDownStatement = statementFactory.create(tearDownClause != null ? tearDownClause : NOP_CLAUSE);
          private Statement tearDownAfterAllStatement = statementFactory.create(tearDownAfterAllClause != null ? tearDownAfterAllClause : NOP_CLAUSE);

          List<IndexedTestCase> testCases = createTestCases(this);

          private List<TestOracle> createTestOracles() {
            AtomicInteger i = new AtomicInteger(0);
            return testOracleBeanList.stream().map((BaseForTestOracle each) -> each.create(i.getAndIncrement(), session, this)).collect(toList());
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
                setUpBeforeAllStatement,
                createMemo());
          }

          @Override
          public Form<Action> getSetUpActionFactory() {
            return createActionFactory("Fixture set up", setUpStatement, createMemo());
          }

          @Override
          public Form<Action> getTearDownActionFactory() {
            return createActionFactory("Fixture tear down", tearDownStatement, createMemo());
          }

          @Override
          public Form<Action> getTearDownAfterAllActionFactory() {
            return createActionFactory(
                format("Suite level tear down: %s", description),
                tearDownAfterAllStatement,
                createMemo()
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

          private Form<Action> createActionFactory(String actionName, Statement statement, Map<List<Object>, Object> memo) {
            return (Stage input) -> {
              Object result =
                  statement == null ?
                      nop() :
                      toFunc(statement, FuncInvoker.create(createMemo())).apply(input);
              return (Action) requireNonNull(
                  result,
                  String.format("statement for '%s' was not valid '%s'", actionName, statement)
              );
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

    public ParameterSpaceDescriptor create(Session session, Statement.Factory statementFactory) {
      return new ParameterSpaceDescriptor() {
        @Override
        public List<Parameter> getParameters() {
          return composeParameters(BaseForFactorSpaceDescriptor.this.parameterDefinitionMap);
        }

        @Override
        public List<Constraint> getConstraints() {
          return constraintList.stream()
              .map((List<Object> each) -> {
                Statement statement = statementFactory.create(each);
                Form<Boolean> form = toFunc(
                    statement,
                    FuncInvoker.create(createMemo())
                );
                return Constraint.create(
                    (Tuple in) -> requireNonNull(form.apply(session.createConstraintConstraintGenerationStage(in))),
                    Statement.Utils.involvedParameters(statement)
                );
              })
              .collect(toList());
        }
      };
    }

    @SuppressWarnings("unchecked")
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
                    return Parameter.Fsm.Factory.of(
                        (Class<? extends FsmSpec<Object>>) forName(Objects.toString(def.args.get(0))),
                        Integer.valueOf(Objects.toString(def.args.get(1)))
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
    public TestOracle create(int index, Session session, TestSuiteDescriptor testSuiteDescriptor) {
      Statement.Factory statementFactory = testSuiteDescriptor.statementFactory();
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
        public Function<Session, Action>
        createTestActionFactory(TestItem testItem, Map<List<Object>, Object> memo) {
          Tuple testCaseTuple = testItem.getTestCaseTuple();
          Report report = session.createReport(testItem);
          return (Session session) -> sequential(
              composeDescription(testCaseTuple),
              createBefore(testItem, report, memo),
              attempt(
                  Actions.<Tuple, TestIO>test("Verify with: " + projectMultiLevelFactors(testCaseTuple))
                      .given(createGiven(testItem, report, session, memo))
                      .when(createWhen(testItem, report, session, memo))
                      .then(createThen(testItem, report, session, memo)).build())
                  .recover(
                      AssertionError.class,
                      onTestFailure(testItem, report, session, memo))
                  .ensure(createAfter(testItem, report, memo))
                  .build()
          );
        }

        private Tuple projectMultiLevelFactors(Tuple testCaseTuple) {
          return Utils.filterSimpleSingleLevelParametersOut(
              testCaseTuple,
              testSuiteDescriptor.getFactorSpaceDescriptor().getParameters()
          );
        }

        private String composeDescription(Tuple testCaseTuple) {
          return template(description, append(testCaseTuple, "@TESTSUITE", testSuiteDescriptor.getDescription()));
        }


        private Action createBefore(TestItem testItem, Report report, Map<List<Object>, Object> memo) {
          return createActionFromClause(BEFORE, beforeClause, testItem, report, memo);
        }

        private Source<Tuple> createGiven(final TestItem testItem, final Report report, final Session session, Map<List<Object>, Object> memo) {
          Tuple testCaseTuple = testItem.getTestCaseTuple();
          return new Source<Tuple>() {
            FuncInvoker funcInvoker = FuncInvoker.create(memo);
            Stage givenStage = session.createOracleLevelStage(GIVEN, testItem, report);
            Statement givenStatement = statementFactory.create(givenClause);

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
              Stage whenStage = session.createOracleLevelStage(WHEN, testItem, report);
              return TestIO.create(
                  testCase,
                  Beans.<Boolean>toFunc(statementFactory.create(whenClause), funcInvoker).apply(whenStage));
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
              Stage thenStage = session.createOracleVerificationStage(testItem, testIO.getOutput(), report);
              assertThat(
                  thenStage,
                  new BaseMatcher<Stage>() {
                    @Override
                    public boolean matches(Object item) {
                      return requireNonNull(
                          Beans.<Boolean>toFunc(statementFactory.create(thenClause), funcInvoker)
                              .apply(thenStage));
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
              Stage onFailureStage = session.createOracleFailureHandlingStage(testItem, input, report);
              Statement onFailureStatement = statementFactory.create(onFailureClause);
              Utils.performActionWithLogging(requireNonNull(
                  onFailureClause != null ?
                      Beans.<Action>toFunc(onFailureStatement, funcInvoker) :
                      (Form<Action>) input1 -> Actions.nop()).apply(onFailureStage));
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
          Stage stage = session.createOracleLevelStage(stageType, testItem, report);
          Statement statement = statementFactory.create(clause);
          FuncInvoker funcInvoker = FuncInvoker.create(memo);
          return Beans.<Action>toFunc(statement, funcInvoker).apply(stage);
        }
      };
    }
  }

  private static <U> Form<U> toFunc(Statement statement, FuncInvoker funcInvoker) {
    //noinspection unchecked
    return (Form) statement.compile(funcInvoker);
  }
}
