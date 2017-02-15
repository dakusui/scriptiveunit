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
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.dakusui.actionunit.Actions.named;
import static com.github.dakusui.actionunit.Actions.sequential;
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
        Statement setUpStatement = new Statement.Factory(driverObject).create(setUpClause);
        Statement setUpBeforeAllStatement = new Statement.Factory(driverObject).create(setUpBeforeAllClause);

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
          return createActionFactory("Fixture set up", "reset fixture setup writer", setUpStatement);
        }

        @Override
        public List<String> getInvolvedParameterNamesInSetUpAction() {
          return Statement.Utils.involvedParameters(setUpStatement);
        }

        @Override
        public Func<Stage, Action> getSetUpBeforeAllActionFactory() {
          return createActionFactory(format("Suite level set up: %s", description), "reset suite level fixture writer", setUpBeforeAllStatement);
        }


        private Func<Stage, Action> createActionFactory(String actionName, String resetMessage, Statement statement) {
          return input -> Actions.sequential(
              actionName,
              Actions.simple(new Runnable() {
                               @Override
                               public void run() {
                                 // TODO "invoker reset" should come here
                               }

                               @Override
                               public String toString() {
                                 return resetMessage;
                               }
                             }
              ),
              statement == null ?
                  Actions.nop() :
                  Beans.<Stage, Action>toFunc(statement).apply(input)
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
                Func<Stage, Boolean> func = toFunc(statement = statementFactory.create(each));
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
        Func<Stage, Boolean> given = toFunc(statementFactory.create(givenClause));
        Func<Stage, Stage> when = toFunc(statementFactory.create(whenClause));
        Func<Stage, Boolean> then = toFunc(statementFactory.create(thenClause));

        @Override
        public String getDescription() {
          return description;
        }

        @Override
        public Action createTestAction(int itemId, String testSuiteDescription, Tuple testCase) {
          return named(format("%03d: %s", itemId, description),
              sequential(
                  Actions.simple(new Runnable() {
                    @Override
                    public void run() {
                      // TODO
                    }

                    @Override
                    public String toString() {
                      return "reset writers";
                    }
                  }),
                  Actions.<Tuple, TestResult>test("verify with: " + Objects.toString(testCase))
                      .given(new Source<Tuple>() {
                        @Override
                        public Tuple apply(Context context) {
                          assumeThat(testCase, new BaseMatcher<Tuple>() {
                            @Override
                            public boolean matches(Object item) {
                              return requireNonNull(given.apply(GIVEN.create(testCase)));
                            }

                            @Override
                            public void describeTo(Description description) {
                              description.appendText(format("test case=%s", testCase));
                            }
                          });
                          return testCase;
                        }

                        @Override
                        public String toString() {
                          return format("%n%s", "TODO"/* TODO */);
                        }
                      })
                      .when(new Pipe<Tuple, TestResult>() {
                        @Override
                        public TestResult apply(Tuple testCase, Context context) {
                          return TestResult.create(testCase, when.apply(WHEN.create(testCase)));
                        }

                        @Override
                        public String toString() {
                          return format("%n%s", "TODO"/* TODO */);
                        }
                      })
                      .then(new Sink<TestResult>() {
                        @Override
                        public void apply(TestResult testResult, Context context) {
                          Stage thenStage = THEN.create(testResult.getTestCase(), testResult.getOutput());
                          assertThat(
                              thenStage,
                              new BaseMatcher<Stage>() {
                                @Override
                                public boolean matches(Object item) {
                                  return requireNonNull(then.apply(thenStage));
                                }

                                @Override
                                public void describeTo(Description description) {
                                  description.appendText(format("output should have made true criterion defined in stage:%s", thenStage.getType()));
                                }

                                @Override
                                public void describeMismatch(Object item, Description description) {
                                  Object output = testResult.getOutput() instanceof Iterable ?
                                      iterableToString((Iterable<?>) testResult.getOutput()) :
                                      testResult.getOutput();
                                  description.appendText(format("output '%s' created from '%s' did not satisfy it", output, testResult.getTestCase()));
                                }
                              }
                          );
                        }

                        @Override
                        public String toString() {
                          return format("%n%s", "TODO"/* TODO */);
                        }
                      }).build()
              ));
        }
      };
    }

  }

  private static <T extends Stage, U> Func<T, U> toFunc(Statement statement) {
    //noinspection unchecked
    return Func.class.<T, U>cast(statement.executeWith(new FuncInvoker.Impl(0)));
  }
}
