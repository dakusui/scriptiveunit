package com.github.dakusui.scriptiveunit.model.desc.testitem;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.session.Report;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import org.hamcrest.Matcher;

import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.core.Utils.append;
import static com.github.dakusui.scriptiveunit.core.Utils.template;

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

  Definition definitionFor(TestItem testItem);

  interface Definition {
    Function<Stage, Action> beforeFactory(TestItem testItem, Report report);

    Function<Stage, Matcher<Tuple>> givenFactory();

    Function<Stage, Object> whenFactory();

    Function<Stage, Sink<AssertionError>> errorHandlerFactory(TestItem testItem, Report report);

    Function<Stage, Function<Object, Matcher<Stage>>> thenFactory();

    Function<Stage, Action> afterFactory(TestItem testItem, Report report);

    String describeTestCase(Tuple testCaseTuple);
  }
}
