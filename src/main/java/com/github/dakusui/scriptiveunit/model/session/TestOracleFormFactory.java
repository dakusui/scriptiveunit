package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.Value;
import com.github.dakusui.scriptiveunit.model.session.action.Sink;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.scriptiveunit.utils.StringUtils.alignLeft;
import static com.github.dakusui.scriptiveunit.utils.StringUtils.indent;
import static com.github.dakusui.scriptiveunit.utils.StringUtils.iterableToString;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public interface TestOracleFormFactory {
  static TestOracleFormFactory createTestOracleFormFactory(TestItem testItem, TestOracle.Definition definition, Function<Tuple, String> testCaseFormatter) {
    Value<Action> beforeValue = definition.before()
        .map(Statement::<Action>toForm)
        .orElse((Stage s) -> nop());
    Value<Boolean> givenValue = definition.given()
        .map(Statement::<Boolean>toForm)
        .orElse((Stage stage) -> true);
    Value<Object> whenValue = (Stage stage) -> definition.when().toForm().apply(stage);
    final Statement thenStatement = definition.then();
    Value<Boolean> thenValue = thenStatement.toForm();
    return new TestOracleFormFactory() {
      @Override
      public Value<Action> beforeFactory() {
        return beforeValue;
      }

      @Override
      public Value<Matcher<Tuple>> givenFactory() {
        return (Stage stage) -> new BaseMatcher<Tuple>() {
          Value.Listener formListener = createFormListener();

          @Override
          public boolean matches(Object item) {
            return givenValue.apply(Stage.Factory.createFormListeningStage(stage, formListener));
          }

          @Override
          public void describeTo(Description description) {
            description.appendText(
                format("<%s>:%n%s",
                    definition.given().map(Statement::format)
                        .orElse("(unavailable)"),
                    formListener.toString()
                ));
          }
        };
      }

      @Override
      public Value<Object> whenFactory() {
        return whenValue;
      }

      @Override
      public Value<Function<Object, Matcher<Stage>>> thenFactory() {
        return stage -> out -> new BaseMatcher<Stage>() {
          Predicate<Stage> p = s -> requireNonNull(thenValue)
              .apply(s);
          Value.Listener formListener = createFormListener();

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
      public Value<Sink<AssertionError>> errorHandlerFactory() {
        return definition.onFailure()
            .map(Statement::<Sink<AssertionError>>toForm)
            .orElse((Stage) -> (AssertionError assertionError, Context context) -> {
              throw assertionError;
            });
      }

      @Override
      public Value<Action> afterFactory() {
        return definition.after()
            .map(Statement::<Action>toForm)
            .orElse((Stage s) -> nop());
      }

      @Override
      public String describeTestCase(Tuple testCaseTuple) {
        return testCaseFormatter.apply(testCaseTuple);
      }

      private Value.Listener createFormListener() {
        return new Value.Listener() {
          class FormLevel {
            Value value;
            int   level;

            FormLevel(Value value, int level) {
              this.value = value;
              this.level = level;
            }
          }

          List<FormLevel> history = new LinkedList<>();
          Map<Value, List<Object>> values = new HashMap<>();

          int level = 0;

          @Override
          public void enter(Value value) {
            this.history.add(new FormLevel(value, level++));
          }

          @Override
          public void leave(Value form, Object value) {
            level--;
            addValue(form, value);
          }

          @Override
          public void fail(Value value, Throwable t) {
            level--;
            addValue(value, t.getMessage());
          }

          @Override
          public String toString() {
            StringBuilder b = new StringBuilder();
            class CountForForm {
              private Map<Value, Integer> map = new HashMap<>();

              private Integer getAndIncrement(Value form) {
                if (!map.containsKey(form))
                  map.put(form, 0);
                int ret = map.get(form);
                try {
                  return ret;
                } finally {
                  map.put(form, ret + 1);
                }
              }
            }
            CountForForm countFor = new CountForForm();
            //noinspection RedundantCast
            history.stream()
                .map(fl -> $(String.format("%s%s", indent(fl.level), fl.value.name()), fl.value))
                .map(v -> $(formatFormName(v[0]), v[1]))
                .map(v -> format("%s:%s",
                    alignLeft((String) v[0], 60),
                    values.get((Value) v[1]).get(countFor.getAndIncrement((Value) v[1]))))
                .forEach((String v) -> b.append(v).append(String.format("%n")));
            return b.toString();
          }

          String formatFormName(Object o) {
            return Objects.toString(o);
          }

          void addValue(Value form, Object value) {
            this.values.computeIfAbsent(form, f -> new LinkedList<>());
            this.values.get(form).add(value);
          }

          Object[] $(Object... values) {
            return values;
          }
        };
      }
    };
  }

  Value<Action> beforeFactory();

  Value<Matcher<Tuple>> givenFactory();

  Value<Object> whenFactory();

  Value<Function<Object, Matcher<Stage>>> thenFactory();

  Value<Sink<AssertionError>> errorHandlerFactory();

  Value<Action> afterFactory();

  String describeTestCase(Tuple testCaseTuple);

}
