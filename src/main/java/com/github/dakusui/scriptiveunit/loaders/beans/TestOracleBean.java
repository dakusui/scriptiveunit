package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracleActionFactory;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormInvoker;
import com.github.dakusui.scriptiveunit.model.form.handle.FormUtils;
import com.github.dakusui.scriptiveunit.model.session.Report;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.utils.TupleUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.Actions.nop;
import static com.github.dakusui.scriptiveunit.model.session.Stage.ExecutionLevel.ORACLE;
import static com.github.dakusui.scriptiveunit.utils.StringUtils.iterableToString;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public abstract class TestOracleBean {
  private final String description;
  private final List<Object> givenClause;
  private final List<Object> whenClause;
  private final List<Object> thenClause;
  private final List<Object> onFailureClause;
  private final List<Object> afterClause;
  private final List<Object> beforeClause;

  protected TestOracleBean(String description, List<Object> beforeClause, List<Object> givenClause, List<Object> whenClause, List<Object> thenClause, List<Object> onFailureClause, List<Object> afterClause) {
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
  public TestOracle create(int index, TestSuiteDescriptor testSuiteDescriptor) {
    Statement.Factory statementFactory = testSuiteDescriptor.statementFactory();
    return new TestOracleImpl(index, description, beforeClause, givenClause, whenClause, thenClause, onFailureClause, afterClause, testSuiteDescriptor, statementFactory);
  }

  public static class TestOracleImpl implements TestOracle {
    private final int index;
    private final TestSuiteDescriptor testSuiteDescriptor;
    private final Statement.Factory statementFactory;
    private List<Object> afterClause;
    private List<Object> beforeClause;
    private String description;
    private List<Object> givenClause;
    private List<Object> onFailureClause;
    private List<Object> thenClause;
    private List<Object> whenClause;

    TestOracleImpl(int index, final String description, final List<Object> beforeClause, final List<Object> givenClause, final List<Object> whenClause, final List<Object> thenClause, final List<Object> onFailureClause, final List<Object> afterClause, TestSuiteDescriptor testSuiteDescriptor, Statement.Factory statementFactory) {
      this.index = index;
      this.testSuiteDescriptor = testSuiteDescriptor;
      this.statementFactory = statementFactory;
      this.afterClause = afterClause;
      this.beforeClause = beforeClause;
      this.description = description;
      this.givenClause = givenClause;
      this.onFailureClause = onFailureClause;
      this.thenClause = thenClause;
      this.whenClause = whenClause;
    }

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

    class TestOracleActionFactoryImpl implements TestOracleActionFactory {
      private final TestItem testItem;

      TestOracleActionFactoryImpl(TestItem testItem) {
        this.testItem = testItem;
      }

      @Override
      public Function<Stage, Action> beforeFactory(TestItem testItem, Report report) {
        return before()
            .map(FormUtils.INSTANCE::<Action>toForm)
            .orElse((Stage s) -> nop());
      }

      public Optional<Statement> before() {
        return Optional.ofNullable(beforeClause)
            .map(statementFactory::create);
      }

      @Override
      public Function<Stage, Matcher<Tuple>> givenFactory() {
        return (Stage s) -> new BaseMatcher<Tuple>() {
          private Statement givenStatement = statementFactory.create(givenClause);
          private FormInvoker formInvoker = FormInvokerImpl.create();

          @Override
          public boolean matches(Object item) {
            return requireNonNull(FormUtils.INSTANCE.<Boolean>toForm(givenStatement).apply(s));
          }

          @Override
          public void describeTo(Description description) {
            description.appendText(
                format("input (%s) should have made true following criterion but not.:%n'%s' defined in stage:%s",
                    testItem.getTestCaseTuple(),
                    formInvoker.asString(),
                    ORACLE));
          }
        };
      }

      @Override
      public Function<Stage, Object> whenFactory() {
        return (Stage s) -> FormUtils.INSTANCE.toForm(statementFactory.create(whenClause)).apply(s);
      }

      @Override
      public Function<Stage, Function<Object, Matcher<Stage>>> thenFactory() {
        Statement thenStatement = statementFactory.create(thenClause);
        FormInvoker formInvoker = FormInvokerImpl.create();
        return stage -> out -> new BaseMatcher<Stage>() {
          Function<FormInvoker, Predicate<Stage>> p = fi -> s -> (Boolean) requireNonNull(
              FormUtils.INSTANCE.toForm(thenStatement).apply(s));
          Function<FormInvoker, Function<Stage, String>> c = fi -> s -> fi.asString();

          @Override
          public boolean matches(Object item) {
            return p.apply(formInvoker).test(stage);
          }

          @Override
          public void describeTo(Description description) {
            description.appendText(format("output should have made true the criterion defined in stage:%s", stage.getExecutionLevel()));
          }

          @Override
          public void describeMismatch(Object item, Description description) {
            Object output = out instanceof Iterable ?
                iterableToString((Iterable<?>) out) :
                out;
            description.appendText(format("output '%s'", output));
            description.appendText(" ");
            if (!testItem.getTestCaseTuple().isEmpty()) {
              description.appendText(format("created from '%s'", testItem.getTestCaseTuple()));
              description.appendText(" ");
            }
            description.appendText(format("did not satisfy it.:%n'%s'", c.apply(formInvoker).apply(stage)));
          }
        };
      }

      @Override
      public String describeTestCase(Tuple testCaseTuple) {
        return "Verify with: " + TupleUtils.filterSimpleSingleLevelParametersOut(
            testCaseTuple,
            testSuiteDescriptor.getFactorSpaceDescriptor().getParameters()
        );
      }

      @Override
      public Function<Stage, Sink<AssertionError>> errorHandlerFactory(TestItem testItem, Report report) {
        Statement onFailureStatement = statementFactory.create(onFailureClause);
        return (Stage s) -> (AssertionError input, Context context) -> requireNonNull(
            onFailureClause != null ?
                FormUtils.INSTANCE.<Action>toForm(onFailureStatement) :
                (Form<Action>) input1 -> nop()).apply(s);
      }

      @Override
      public Function<Stage, Action> afterFactory(TestItem testItem, Report report) {
        if (afterClause == null)
          return s -> Actions.nop();
        Statement statement = statementFactory.create(afterClause);
        return s -> FormUtils.INSTANCE.<Action>toForm(statement).apply(s);
      }

    }

    public TestOracleActionFactory testOracleActionFactoryFor(TestItem testItem) {
      return new TestOracleActionFactoryImpl(testItem);
    }
  }
}
