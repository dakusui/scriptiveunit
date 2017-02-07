package com.github.dakusui.scriptunit.loaders;

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
import com.github.dakusui.scriptunit.ScriptRunner.Type;
import com.github.dakusui.scriptunit.core.Utils;
import com.github.dakusui.scriptunit.model.*;
import com.github.dakusui.scriptunit.model.statement.Form;
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
import static com.github.dakusui.scriptunit.core.Utils.toCamelCase;
import static com.github.dakusui.scriptunit.exceptions.ScriptUnitException.wrap;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;

public enum Beans {
  ;

  public abstract static class BaseForTestSuiteDescriptor {
    final BaseForCoveringArrayEngineConfig  coveringArrayEngineConfigBean;
    final BaseForFactorSpaceDescriptor      factorSpaceBean;
    final List<? extends BaseForTestOracle> testOracleBeanList;
    final String                            description;
    final Type                              runnerType;

    public BaseForTestSuiteDescriptor(BaseForCoveringArrayEngineConfig coveringArrayEngineConfigBean, BaseForFactorSpaceDescriptor factorSpaceBean, List<? extends BaseForTestOracle> testOracleBeanList, String description, String runnerType) {
      this.coveringArrayEngineConfigBean = coveringArrayEngineConfigBean != null ?
          coveringArrayEngineConfigBean :
          new BaseForCoveringArrayEngineConfig() {
            @Override
            public CoveringArrayEngineConfig create() {
              return super.create();
            }
          }
      ;
      this.runnerType = Type.valueOf(Utils.toALL_CAPS(runnerType != null ? runnerType: toCamelCase(Type.GROUP_BY_TEST_ORACLE.name())));
      this.factorSpaceBean = factorSpaceBean;
      this.testOracleBeanList = testOracleBeanList;
      this.description = description;
    }


    public TestSuiteDescriptor create(Class<?> driverClass) {
      try {
        return create(
            new Statement.Factory(
                new Form.Factory(driverClass.newInstance())
            )
        );
      } catch (InstantiationException | IllegalAccessException e) {
        throw wrap(e);
      }
    }

    public TestSuiteDescriptor create(Statement.Factory statementFactory) {
      return new TestSuiteDescriptor() {
        @Override
        public String getDescription() {
          return description;
        }

        @Override
        public FactorSpaceDescriptor getFactorSpaceDescriptor() {
          return factorSpaceBean.create(statementFactory);
        }

        @Override
        public CoveringArrayEngineConfig getCoveringArrayEngineConfig() {
          return coveringArrayEngineConfigBean.create();
        }

        @Override
        public List<? extends TestOracle> getTestOracles() {
          return testOracleBeanList.stream().map(each -> each.create(statementFactory)).collect(Collectors.toList());
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
    private final String                               className;

    BaseForCoveringArrayEngineConfig() {
      this("com.github.dakusui.jcunit.plugins.caengines.IpoGcCoveringArrayEngine", singletonList(2));
    }

    public BaseForCoveringArrayEngineConfig(String className, List<Object> options) {
      try {
        this.className = className;
        Class clazz = Class.class.cast(Class.forName(this.className));
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
                Func.Invoker invoker = new Func.Invoker(0);
                //noinspection unchecked
                Func<Stage, Boolean> func = (Func<Stage, Boolean>) statementFactory.create(
                    each, invoker
                ).execute();
                return new TestSuite.Predicate("(constraint)", computeInvolvedParameters(invoker)) {
                  @Override
                  public boolean apply(Tuple in) {
                    return requireNonNull(func.apply(new Stage() {
                      @Override
                      public Tuple getFixture() {
                        return in;
                      }

                      @Override
                      public <RESPONSE> RESPONSE response() {
                        throw new UnsupportedOperationException();
                      }

                      @Override
                      public Type getType() {
                        return Type.GIVEN;
                      }
                    }));
                  }
                };
              })
              .collect(Collectors.toList());
        }

        private String[] computeInvolvedParameters(Func.Invoker invoker) {
          return invoker.getInvolvedParameterNames().toArray(new String[0]);
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
    private final List<Object> given;
    private final List<Object> when;
    private final List<Object> then;

    public BaseForTestOracle(String description, List<Object> given, List<Object> when, List<Object> then) {
      this.description = description;
      this.given = given;
      this.when = when;
      this.then = then;
    }

    /**
     * Test oracles created by this method are not thread safe since invokers ({@code Func.Invoker}
     * objects) have their internal states and not created every time the oracles
     * are performed.
     *
     * @param statementFactory A factory that creates {@code Statement} objects.
     */
    public TestOracle create(Statement.Factory statementFactory) {
      //noinspection unchecked,Guava
      return new TestOracle() {
        Func.Invoker invokerForGiven, invokerForWhen, invokerForThen;
        Func<Stage, Boolean> given = createFunc(
            BaseForTestOracle.this.given,
            invokerForGiven = new Func.Invoker(0));
        Func<Stage, Stage> when = createFunc(
            BaseForTestOracle.this.when,
            invokerForWhen = new Func.Invoker(0));
        Func<Stage, Boolean> then = createFunc(
            BaseForTestOracle.this.then,
            invokerForThen = new Func.Invoker(0));

        @Override
        public Action createTestAction(String testSuiteDescription, int testCaseId, TestCase testCase) {
          return named(format("%s: %s", testCase.getCategory(), description),
              sequential(
                  Actions.simple(new Runnable() {
                    @Override
                    public void run() {
                      invokerForGiven.reset();
                      invokerForWhen.reset();
                      invokerForThen.reset();
                    }

                    @Override
                    public String toString() {
                      return "reset writers";
                    }
                  }),
                  Actions.<TestCase, TestResult>test("verify with: " + Objects.toString(testCase.getTuple()))
                      .given(new Source<TestCase>() {
                        @Override
                        public TestCase apply(Context context) {
                          assumeThat(testCase, new BaseMatcher<TestCase>() {
                            @Override
                            public boolean matches(Object item) {
                              return requireNonNull(given.apply(Stage.Type.GIVEN.create(testCase.getTuple())));
                            }

                            @Override
                            public void describeTo(Description description) {
                              description.appendText(format("tag=%s; test case=%s", testCase.getCategory(), testCase.getTuple()));
                            }
                          });
                          return testCase;
                        }

                        @Override
                        public String toString() {
                          return format("%n%s", invokerForGiven.asString());
                        }
                      })
                      .when(new Pipe<TestCase, TestResult>() {
                        @Override
                        public TestResult apply(TestCase testCase, Context context) {
                          return TestResult.create(testCase, when.apply(Stage.Type.WHEN.create(testCase.getTuple())));
                        }

                        @Override
                        public String toString() {
                          return format("%n%s", invokerForWhen.asString());
                        }
                      })
                      .then(new Sink<TestResult>() {
                        @Override
                        public void apply(TestResult input, Context context) {
                          Stage thenStage = Stage.Type.THEN.create(input.getTestCase().getTuple(), input.getOutput());
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
                                  description.appendText(format("output '%s' created from '%s' did not satisfy it", input.getOutput(), input.getTestCase()));
                                }
                              }
                          );
                        }

                        @Override
                        public String toString() {
                          return format("%n%s", invokerForThen.asString());
                        }
                      }).build()
              ));
        }

        private <T extends Stage, U> Func createFunc(List<Object> clause, Func.Invoker invoker) {
          return Func.class.<T, U>cast(
              statementFactory.create(
                  clause,
                  invoker
              ).execute());
        }
      };
    }

  }
}
