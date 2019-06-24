package com.github.dakusui.scriptiveunit.model.desc.testitem;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.Report;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import org.hamcrest.Matcher;

import java.util.function.Function;

public interface TestOracleFormFactory {
  Form<Action> beforeFactory(TestItem testItem, Report report);

  Form<Matcher<Tuple>> givenFactory();

  Form<Object> whenFactory();

  Form<Function<Object, Matcher<Stage>>> thenFactory();

  Form<Sink<AssertionError>> errorHandlerFactory(TestItem testItem, Report report);

  Form<Action> afterFactory(TestItem testItem, Report report);

  String describeTestCase(Tuple testCaseTuple);
}
