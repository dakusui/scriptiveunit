package com.github.dakusui.scriptiveunit.model.desc.testitem;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormInvoker;
import com.github.dakusui.scriptiveunit.model.form.handle.FormUtils;
import com.github.dakusui.scriptiveunit.model.session.Report;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
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
import static com.github.dakusui.scriptiveunit.utils.StringUtils.template;
import static com.github.dakusui.scriptiveunit.utils.TupleUtils.append;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public interface TestOracle {
  static String templateTestOracleDescription(TestOracle testOracle, Tuple testCaseTuple, String testSuiteDescription) {
    return template(testOracle.getDescription(), append(testCaseTuple, "@TESTSUITE", testSuiteDescription));
  }

  int getIndex();

  /**
   * Returns a string that describes this test oracle.
   * <p>
   * Note that this method always returns raw form (a string before being templated).
   */
  String getDescription();

  String templateDescription(Tuple testCaseTuple, String testSuiteDescription);

  TestOracleFormFactory testOracleActionFactoryFor(TestItem testItem);

  interface Definition {
    Optional<Statement> before();

    Optional<Statement> given();

    Statement when();

    Statement then();

    Optional<Statement> onFailure();

    Optional<Statement> after();

    static Definition create(Statement.Factory statementFactory, List<Object> before, List<Object> given, List<Object> when, List<Object> then, List<Object> onFailure, List<Object> after) {
      requireNonNull(statementFactory);
      requireNonNull(when);
      requireNonNull(then);
      return new Definition() {
        @Override
        public Optional<Statement> before() {
          return Optional.ofNullable(before)
              .map(statementFactory::create);
        }

        @Override
        public Optional<Statement> given() {
          return Optional.ofNullable(given)
              .map(statementFactory::create);
        }

        @Override
        public Statement when() {
          return statementFactory.create(when);
        }

        @Override
        public Statement then() {
          return statementFactory.create(then);
        }

        @Override
        public Optional<Statement> onFailure() {
          return Optional.ofNullable(onFailure)
              .map(statementFactory::create);
        }

        @Override
        public Optional<Statement> after() {
          return Optional.ofNullable(after)
              .map(statementFactory::create);
        }
      };
    }
  }

  static TestOracleFormFactory createTestOracleFormFactory(TestItem testItem, Definition definition, Function<Tuple, String> testCaseFormatter) {
    return new TestOracleFormFactory() {
      @Override
      public Form<Action> beforeFactory(TestItem testItem_, Report report) {
        return definition.before()
            .map(FormUtils.INSTANCE::<Action>toForm)
            .orElse((Stage s) -> nop());
      }

      @Override
      public Form<Matcher<Tuple>> givenFactory() {
        return (Stage s) -> new BaseMatcher<Tuple>() {
          private FormInvoker formInvoker = FormInvoker.create();

          @Override
          public boolean matches(Object item) {
            return definition.given()
                .map(FormUtils.INSTANCE::<Boolean>toForm)
                .orElse((Stage stage) -> true)
                .apply(s);
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
      public Form<Object> whenFactory() {
        return (Stage stage) -> FormUtils.INSTANCE.toForm(definition.when()).apply(stage);
      }

      @Override
      public Form<Function<Object, Matcher<Stage>>> thenFactory() {
        FormInvoker formInvoker = FormInvoker.create();
        return stage -> out -> new BaseMatcher<Stage>() {
          Function<FormInvoker, Predicate<Stage>> p = fi -> s -> (Boolean) requireNonNull(
              FormUtils.INSTANCE.toForm(definition.then()).apply(s));
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
      public Form<Sink<AssertionError>> errorHandlerFactory(TestItem testItem, Report report) {
        return definition.onFailure()
            .map(FormUtils.INSTANCE::<Sink<AssertionError>>toForm)
            .orElse((Stage) -> (AssertionError assertionError, Context context) -> {
              throw assertionError;
            });
      }

      @Override
      public Form<Action> afterFactory(TestItem testItem_, Report report) {
        return definition.after()
            .map(FormUtils.INSTANCE::<Action>toForm)
            .orElse((Stage s) -> nop());
      }

      @Override
      public String describeTestCase(Tuple testCaseTuple) {
        return testCaseFormatter.apply(testCaseTuple);
      }
    };
  }
}
