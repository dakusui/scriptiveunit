package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormInvoker;
import com.github.dakusui.scriptiveunit.model.session.Report;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.Actions.nop;
import static com.github.dakusui.scriptiveunit.core.Utils.iterableToString;
import static com.github.dakusui.scriptiveunit.model.form.FormInvoker.createMemo;
import static com.github.dakusui.scriptiveunit.model.session.Stage.ExecutionLevel.ORACLE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public abstract class TestOracleBean {
  private final String       description;
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
    private final int                 index;
    private final TestSuiteDescriptor testSuiteDescriptor;
    private final Statement.Factory   statementFactory;
    private       List<Object>        afterClause;
    private       List<Object>        beforeClause;
    private       String              description;
    private       List<Object>        givenClause;
    private       List<Object>        onFailureClause;
    private       List<Object>        thenClause;
    private       List<Object>        whenClause;

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

    class MyDefinition implements Definition {
      private final TestItem         testItem;
      private       FormInvoker.Memo memo = createMemo();

      MyDefinition(TestItem testItem) {
        this.testItem = testItem;
      }

      @Override
      public Function<Stage, Action> beforeFactory(TestItem testItem, Report report) {
        if (beforeClause == null)
          return s -> Actions.nop();
        Statement statement = statementFactory.create(beforeClause);
        FormInvoker formInvoker = FormInvoker.create(memo);
        return s -> BeanUtils.<Action>toForm(statement, formInvoker).apply(s);
      }

      @Override
      public Function<Stage, Matcher<Tuple>> givenFactory() {
        return (Stage s) -> new BaseMatcher<Tuple>() {
          private Statement givenStatement = statementFactory.create(givenClause);
          private FormInvoker formInvoker = FormInvoker.create(memo);

          @Override
          public boolean matches(Object item) {
            return requireNonNull(BeanUtils.<Boolean>toForm(givenStatement, formInvoker).apply(s));
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
        return new Function<Stage, Object>() {
          FormInvoker formInvoker = FormInvoker.create(memo);

          @Override
          public Object apply(Stage s) {
            return BeanUtils.<Boolean>toForm(statementFactory.create(whenClause), formInvoker).apply(s);
          }
        };
      }

      @Override
      public Function<Stage, Function<Object, Matcher<Stage>>> thenFactory() {
        Statement thenStatement = statementFactory.create(thenClause);
        FormInvoker formInvoker = FormInvoker.create(memo);
        return stage -> out -> new BaseMatcher<Stage>() {
          Function<FormInvoker, Predicate<Stage>> p = fi -> s -> requireNonNull(
              BeanUtils.<Boolean>toForm(thenStatement, fi).apply(s));
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
            description.appendText(String.format("output '%s' created from '%s' did not satisfy it.:%n'%s'",
                output,
                testItem.getTestCaseTuple(),
                c.apply(formInvoker).apply(stage)));
          }
        };
      }

      @Override
      public String describeTestCase(Tuple testCaseTuple) {
        return "Verify with: " + Utils.filterSimpleSingleLevelParametersOut(
            testCaseTuple,
            testSuiteDescriptor.getFactorSpaceDescriptor().getParameters()
        );
      }

      @Override
      public Function<Stage, Sink<AssertionError>> errorHandlerFactory(TestItem testItem, Report report) {
        Statement onFailureStatement = statementFactory.create(onFailureClause);
        FormInvoker formInvoker = FormInvoker.create(memo);
        return (Stage s) -> (AssertionError input, Context context) -> requireNonNull(
            onFailureClause != null ?
                BeanUtils.<Action>toForm(onFailureStatement, formInvoker) :
                (Form<Action>) input1 -> nop()).apply(s);
      }

      @Override
      public Function<Stage, Action> afterFactory(TestItem testItem, Report report) {
        if (afterClause == null)
          return s -> Actions.nop();
        Statement statement = statementFactory.create(afterClause);
        FormInvoker formInvoker = FormInvoker.create(memo);
        return s -> BeanUtils.<Action>toForm(statement, formInvoker).apply(s);
      }

    }

    public Definition definitionFor(TestItem testItem) {
      return new MyDefinition(testItem) {
      };
    }

  }
}
