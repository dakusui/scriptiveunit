package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.action.Sink;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.scriptiveunit.utils.StringUtils.indent;
import static com.github.dakusui.scriptiveunit.utils.StringUtils.iterableToString;
import static com.github.dakusui.scriptiveunit.utils.StringUtils.spaces;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
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
        return (Stage stage) -> new BaseMatcher<Tuple>() {
          Form.Listener formListener = createFormListener();

          @Override
          public boolean matches(Object item) {
            return givenForm.apply(Stage.Factory.createFormListeningStage(stage, formListener));
          }

          @Override
          public void describeTo(Description description) {
            description.appendText(
                format("Precondition:<%s> was not satisfied by given input:<%s>:%n%s",
                    definition.given().map(Statement::format)
                        .orElse("(unavailable)"),
                    testItem.getTestCaseTuple(),
                    formListener.toString()
                ));
          }
        };
      }

      @Override
      public Form<Object> whenFactory() {
        return whenForm;
      }

      private Form.Listener createFormListener() {
        return new Form.Listener() {
          List<StringBuilder> b = new LinkedList<StringBuilder>() {{
          }};
          int indentLevel = 0;
          List<Object> out = new LinkedList<>();

          @Override
          public void enter(Form form) {
            if (currentLineLength().isPresent()) {
              cur().append(pad());
              cur().append(format("%s", out.isEmpty() ? "" : out.toString()));
              out.removeAll(unmodifiableList(out));
            }
            b.add(new StringBuilder());
            String call = format("%s(%s", indent(indentLevel), form.name());
            cur().append(call);
            indentLevel++;
          }

          @Override
          public void leave(Form form, Object value) {
            cur().append(")");
            out.add(value);
          }

          @Override
          public void fail(Form form, Throwable t) {
            indentLevel--;
            cur().append(format("): '%s'%n", t.getMessage()));
          }

          @Override
          public String toString() {
            return String.join(format("%n"), b) + pad() + out;
          }

          private StringBuilder cur() {
            return this.b.get(b.size() - 1);
          }

          private OptionalInt currentLineLength() {
            return b.size() > 0 ?
                OptionalInt.of(b.get(b.size() - 1).length()) :
                OptionalInt.empty();
          }

          private String pad() {
            return spaces(60 - currentLineLength().orElse(60));
          }
        };
      }

      @Override
      public Form<Function<Object, Matcher<Stage>>> thenFactory() {
        return stage -> out -> new BaseMatcher<Stage>() {
          Predicate<Stage> p = s -> requireNonNull(thenForm)
              .apply(s);
          Form.Listener formListener = createFormListener();

          @Override
          public boolean matches(Object item) {
            return p.test(Stage.Factory.createFormListeningStage(stage, formListener));
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
            description.appendText(format("did not satisfy it.:%n%s", formListener.toString()));
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
