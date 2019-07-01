package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.action.Sink;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.scriptiveunit.utils.StringUtils.iterableToString;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public interface TestOracleFormFactory {
  static TestOracleFormFactory createTestOracleFormFactory(TestItem testItem, TestOracle.Definition definition, Function<Tuple, String> testCaseFormatter) {
    Form<Action> beforeForm = definition.before()
        .map(Statement::<Action>toForm)
        .orElse((Stage s) -> nop());
    Form<Boolean> givenForm = definition.given()
        .map(Statement::<Boolean>toForm)
        .orElse((Stage stage) -> true);
    Form<Object> whenForm = (Stage stage) -> definition.when().toForm().apply(stage);
    final Statement thenStatement = definition.then();
    Form<Boolean> thenForm = thenStatement.toForm();
    return new TestOracleFormFactory() {
      @Override
      public Form<Action> beforeFactory() {
        return beforeForm;
      }

      @Override
      public Form<Matcher<Tuple>> givenFactory() {
        return (Stage s) -> new BaseMatcher<Tuple>() {
          @Override
          public boolean matches(Object item) {
            return givenForm.apply(s);
          }

          @Override
          public void describeTo(Description description) {
            description.appendText(
                format("Precondition:<%s> was not satisfied by given input:<%s>",
                    definition.given().map(Statement::format)
                        .orElse("(unavailable)"),
                    testItem.getTestCaseTuple()
                ));
          }
        };
      }

      @Override
      public Form<Object> whenFactory() {
        return whenForm;
      }

      Form.Listener formListener = new Form.Listener() {
        @Override
        public void enter(Form form) {

        }

        @Override
        public void leave(Form form, Object value) {

        }

        @Override
        public void fail(Form form, Throwable t) {

        }
      };

      @Override
      public Form<Function<Object, Matcher<Stage>>> thenFactory() {
        return stage -> out -> new BaseMatcher<Stage>() {
          Predicate<Stage> p = s -> requireNonNull(thenForm).apply(new Stage() {
            @Override
            public ExecutionLevel getExecutionLevel() {
              return s.getExecutionLevel();
            }

            @Override
            public Config getConfig() {
              return s.getConfig();
            }

            @Override
            public int sizeOfArguments() {
              return s.sizeOfArguments();
            }

            @Override
            public <T> T getArgument(int index) {
              return s.getArgument(index);
            }

            @Override
            public Optional<Throwable> getThrowable() {
              return s.getThrowable();
            }

            @Override
            public Optional<Tuple> getTestCaseTuple() {
              return s.getTestCaseTuple();
            }

            @Override
            public <RESPONSE> Optional<RESPONSE> response() {
              return s.response();
            }

            @Override
            public Optional<Report> getReport() {
              return s.getReport();
            }

            @Override
            public Optional<TestItem> getTestItem() {
              return s.getTestItem();
            }

            @Override
            public void enter(Form form) {
              formListener.enter(form);
            }

            @Override
            public void leave(Form form, Object value) {
              formListener.leave(form, value);
            }

            @Override
            public void fail(Form form, Throwable t) {
              formListener.fail(form, t);
            }
          });

          @Override
          public boolean matches(Object item) {
            return p.test(stage);
          }

          @Override
          public void describeTo(Description description) {
            description.appendText(format("output:<%s> should have made true the criterion:<%s>",
                out,
                Statement.format(thenStatement)));
          }

          @Override
          public void describeMismatch(Object stage, Description description) {
            Object output = out instanceof Iterable ?
                iterableToString((Iterable<?>) out) :
                out;
            description.appendText(format("output <%s>", output));
            description.appendText(" ");
            if (!testItem.getTestCaseTuple().isEmpty()) {
              description.appendText(format("created from <%s>", testItem.getTestCaseTuple()));
              description.appendText(" ");
            }
            description.appendText(format("did not satisfy it.:%n"));
          }
        };
      }

      @Override
      public Form<Sink<AssertionError>> errorHandlerFactory() {
        return definition.onFailure()
            .map(Statement::<Sink<AssertionError>>toForm)
            .orElse((Stage) -> (AssertionError assertionError, Context context) -> {
              throw assertionError;
            });
      }

      @Override
      public Form<Action> afterFactory() {
        return definition.after()
            .map(Statement::<Action>toForm)
            .orElse((Stage s) -> nop());
      }

      @Override
      public String describeTestCase(Tuple testCaseTuple) {
        return testCaseFormatter.apply(testCaseTuple);
      }
    };
  }

  Form<Action> beforeFactory();

  Form<Matcher<Tuple>> givenFactory();

  Form<Object> whenFactory();

  Form<Function<Object, Matcher<Stage>>> thenFactory();

  Form<Sink<AssertionError>> errorHandlerFactory();

  Form<Action> afterFactory();

  String describeTestCase(Tuple testCaseTuple);
}
