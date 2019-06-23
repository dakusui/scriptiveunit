package com.github.dakusui.scriptiveunit.model.desc.testitem;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.session.Report;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import org.hamcrest.Matcher;

import java.util.function.Function;

public interface TestOracleActionFactory {
  Function<Stage, Action> beforeFactory(TestItem testItem, Report report);

  Function<Stage, Matcher<Tuple>> givenFactory();

  Function<Stage, Object> whenFactory();

  Function<Stage, Function<Object, Matcher<Stage>>> thenFactory();

  Function<Stage, Sink<AssertionError>> errorHandlerFactory(TestItem testItem, Report report);

  Function<Stage, Action> afterFactory(TestItem testItem, Report report);

  String describeTestCase(Tuple testCaseTuple);
}
