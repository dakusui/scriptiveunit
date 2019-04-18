package com.github.dakusui.scriptiveunit.unittests.issues;

import com.github.dakusui.crest.core.Matcher;
import com.github.dakusui.crest.matcherbuilders.AsList;
import com.github.dakusui.crest.matcherbuilders.AsObject;
import com.github.dakusui.scriptiveunit.drivers.actions.Basic;
import com.github.dakusui.scriptiveunit.examples.Qapi;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import com.github.dakusui.scriptiveunit.testutils.TestUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.asObject;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.Crest.call;
import static com.github.dakusui.crest.Crest.sublistAfter;
import static com.github.dakusui.crest.utils.printable.Predicates.equalTo;
import static com.github.dakusui.crest.utils.printable.Predicates.isEmpty;
import static com.github.dakusui.crest.utils.printable.Predicates.isTrue;
import static com.github.dakusui.crest.utils.printable.Predicates.startsWith;
import static java.util.Arrays.asList;

@RunWith(Enclosed.class)
public class Issue37Test {
  public abstract static class Base extends TestBase {
    public static class TestResult {
      final Result       junitResult;
      final List<String> output;

      TestResult(Result junitResult, List<String> output) {
        this.junitResult = junitResult;
        this.output = output;
      }

      /**
       * This method is invoked reflectively.
       */
      @SuppressWarnings({ "unused", "WeakerAccess" })
      public List<String> getOutput() {
        return this.output;
      }

      /**
       * This method is invoked reflectively.
       */
      @SuppressWarnings("unused")
      public Result getJunitResult() {
        return this.junitResult;
      }
    }

    @Test
    public void test() {
      assertTestResult(runTests());
    }

    private void assertTestResult(TestResult actual) {
      actual.getOutput().forEach(System.out::println);
      assertThat(
          actual,
          allOf(
              junitResultMatcher(asObject("getJunitResult")),
              outputMatcher(asListOf(String.class, call("getOutput").$()))
          ));
    }

    abstract Matcher<? super Object> junitResultMatcher(AsObject<Object, Object> junitResultMatcherBuilder);

    abstract Matcher<? super Object> outputMatcher(AsList<Object, String> outputMatcherBuilder);


    TestResult runTests() {
      TestUtils.configureScriptNameSystemProperty(getScriptName(), Qapi.class);
      List<String> out = new LinkedList<>();
      Basic.setOut(out::add);
      return new TestResult(JUnitCore.runClasses(Qapi.class), out);
    }

    public abstract String getScriptName();
  }

  /**
   * <pre>
   * fireTestStarted :000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :000: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * </pre>
   */
  public static class GroupByTestOracle extends Base {
    @Override
    public String getScriptName() {
      return "tests/issues/issue-37-groupByTestOracle.json";
    }

    Matcher<? super Object> junitResultMatcher(AsObject<Object, Object> junitResultMatcherBuilder) {
      return junitResultMatcherBuilder
          .check(call("wasSuccessful").$(), isTrue())
          .check(call("getFailureCount").$(), equalTo(0))
          .$();
    }

    Matcher<? super Object> outputMatcher(AsList<Object, String> outputMatcherBuilder) {
      return outputMatcherBuilder.check(
          sublistAfter(startsWith("setUpBeforeAll"))
              .after(startsWith("setUp:base=a")).after(startsWith("beforeOracleA:num=2,base=a")).after(startsWith("givenOracleA:false")).after(startsWith("afterOracleA:num=2,base=a"))
              .after(startsWith("tearDown:base=a"))
              .after(startsWith("setUp:base=b")).after(startsWith("givenOracleA:false"))
              .after(startsWith("tearDown:base=b"))
              .after(startsWith("setUp:base=a")).after(startsWith("givenOracleA:false"))
              .after(startsWith("tearDown:base=a"))
              .after(startsWith("setUp:base=b")).after(startsWith("givenOracleA:false"))
              .after(startsWith("tearDown:base=b"))
              .after(startsWith("setUp:base=a")).after(startsWith("beforeOracleB:num=2,base=a")).after(startsWith("givenOracleB:true")).after(startsWith("whenOracleB:HELLO")).after(startsWith("thenOracleB:true")).after(startsWith("afterOracleB:num=2,base=a"))
              .after(startsWith("tearDown:base=a"))
              .after(startsWith("setUp:base=b")).after(startsWith("whenOracleB:HELLO"))
              .after(startsWith("tearDown:base=b"))
              .after(startsWith("setUp:base=a")).after(startsWith("whenOracleB:HELLO"))
              .after(startsWith("tearDown:base=a"))
              .after(startsWith("setUp:base=b")).after(startsWith("whenOracleB:HELLO"))
              .after(startsWith("tearDown:base=b"))
              .after(startsWith("tearDownAfterAll"))
              .$(),
          isEmpty())
          .$();
    }
  }

  /**
   * <pre>
   * fireTestStarted :000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :000: Oracle A[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle A[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle B[1](com.github.dakusui.scriptiveunit.examples.Qapi)
   * </pre>
   */
  public static class GroupByTestCase extends Base {
    @Override
    Matcher<? super Object> junitResultMatcher(AsObject<Object, Object> junitResultMatcherBuilder) {
      return junitResultMatcherBuilder
          .check(call("wasSuccessful").$(), isTrue())
          .check(call("getFailureCount").$(), equalTo(0))
          .$();
    }

    @Override
    Matcher<? super Object> outputMatcher(AsList<Object, String> outputMatcherBuilder) {
      return outputMatcherBuilder.equalTo(
          asList(
              "setUpBeforeAll:null",
              "setUp:base=a",
              "beforeOracleA:num=2,base=a",
              "givenOracleA:false",
              "afterOracleA:num=2,base=a",
              "beforeOracleB:num=2,base=a",
              "givenOracleB:true",
              "whenOracleB:HELLO",
              "thenOracleB:true",
              "afterOracleB:num=2,base=a",
              "tearDown:base=a",
              "setUp:base=b",
              "beforeOracleA:num=2,base=b",
              "givenOracleA:false",
              "afterOracleA:num=2,base=b",
              "beforeOracleB:num=2,base=b",
              "givenOracleB:true",
              "whenOracleB:HELLO",
              "thenOracleB:true",
              "afterOracleB:num=2,base=b",
              "tearDown:base=b",
              "setUp:base=a",
              "beforeOracleA:num=10,base=a",
              "givenOracleA:false",
              "afterOracleA:num=10,base=a",
              "beforeOracleB:num=10,base=a",
              "givenOracleB:true",
              "whenOracleB:HELLO",
              "thenOracleB:true",
              "afterOracleB:num=10,base=a",
              "tearDown:base=a",
              "setUp:base=b",
              "beforeOracleA:num=10,base=b",
              "givenOracleA:false",
              "afterOracleA:num=10,base=b",
              "beforeOracleB:num=10,base=b",
              "givenOracleB:true",
              "whenOracleB:HELLO",
              "thenOracleB:true",
              "afterOracleB:num=10,base=b",
              "tearDown:base=b",
              "tearDownAfterAll:null"
          )).$();
    }

    @Override
    public String getScriptName() {
      return "tests/issues/issue-37-groupByTestCase.json";
    }
  }

  /**
   * <pre>
   * fireTestStarted :000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :002: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:002: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :003: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:003: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * </pre>
   */
  public static class GroupByTestFixture extends Base {
    @Override
    Matcher<? super Object> junitResultMatcher(AsObject<Object, Object> junitResultMatcherBuilder) {
      return junitResultMatcherBuilder
          .check(call("wasSuccessful").$(), isTrue())
          .check(call("getFailureCount").$(), equalTo(0))
          .$();
    }

    @Override
    Matcher<? super Object> outputMatcher(AsList<Object, String> outputMatcherBuilder) {
      return outputMatcherBuilder
          .equalTo(asList(
              "setUpBeforeAll:null",
              "setUp:base=a",                   // BEGIN fixture:base=a
              "beforeOracleA:num=2,base=a",     //   TestCase num=2: OracleA
              "givenOracleA:false",
              "afterOracleA:num=2,base=a",
              "beforeOracleB:num=2,base=a",     //   TestCase num=2: OracleB
              "givenOracleB:true",
              "whenOracleB:HELLO",
              "thenOracleB:true",
              "afterOracleB:num=2,base=a",
              "beforeOracleA:num=10,base=a",    //   TestCase num=10: OracleA
              "givenOracleA:false",
              "afterOracleA:num=10,base=a",
              "beforeOracleB:num=10,base=a",    //   TestCase num=2: OracleB
              "givenOracleB:true",
              "whenOracleB:HELLO",
              "thenOracleB:true",
              "afterOracleB:num=10,base=a",
              "tearDown:base=a",                // END fixture:base=a
              "setUp:base=b",                   // BEGIN fixture:base=b
              "beforeOracleA:num=2,base=b",     //   TestCase num=2: OracleB
              "givenOracleA:false",
              "afterOracleA:num=2,base=b",
              "beforeOracleB:num=2,base=b",     //   TestCase num=2: OracleB
              "givenOracleB:true",
              "whenOracleB:HELLO",
              "thenOracleB:true",
              "afterOracleB:num=2,base=b",
              "beforeOracleA:num=10,base=b",    //   TestCase num=10: OracleA
              "givenOracleA:false",
              "afterOracleA:num=10,base=b",
              "beforeOracleB:num=10,base=b",    //   TestCase num=10: OracleB
              "givenOracleB:true",
              "whenOracleB:HELLO",
              "thenOracleB:true",
              "afterOracleB:num=10,base=b",
              "tearDown:base=b",                // END fixture:base=b
              "tearDownAfterAll:null"
          )).$();
    }

    @Override
    public String getScriptName() {
      return "tests/issues/issue-37-groupByTestFixture.json";
    }
  }

  /**
   * <pre>
   * fireTestStarted :000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:000: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :001: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:001: Oracle A[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :002: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:002: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestStarted :003: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * fireTestFinished:003: Oracle B[0](com.github.dakusui.scriptiveunit.examples.Qapi)
   * </pre>
   */
  public static class GroupByTestFixtureOreringByTestOracle extends Base {
    @Override
    Matcher<? super Object> junitResultMatcher(AsObject<Object, Object> junitResultMatcherBuilder) {
      return junitResultMatcherBuilder
          .check(call("wasSuccessful").$(), isTrue())
          .check(call("getFailureCount").$(), equalTo(0))
          .$();
    }

    @Override
    Matcher<? super Object> outputMatcher(AsList<Object, String> outputMatcherBuilder) {
      return outputMatcherBuilder.equalTo(asList(
          "setUpBeforeAll:null",
          "setUp:base=a",
          "beforeOracleA:num=2,base=a",
          "givenOracleA:false",
          "afterOracleA:num=2,base=a",
          "beforeOracleA:num=10,base=a",
          "givenOracleA:false",
          "afterOracleA:num=10,base=a",
          "beforeOracleB:num=2,base=a",
          "givenOracleB:true",
          "whenOracleB:HELLO",
          "thenOracleB:true",
          "afterOracleB:num=2,base=a",
          "beforeOracleB:num=10,base=a",
          "givenOracleB:true",
          "whenOracleB:HELLO",
          "thenOracleB:true",
          "afterOracleB:num=10,base=a",
          "tearDown:base=a",
          "setUp:base=b",
          "beforeOracleA:num=2,base=b",
          "givenOracleA:false",
          "afterOracleA:num=2,base=b",
          "beforeOracleA:num=10,base=b",
          "givenOracleA:false",
          "afterOracleA:num=10,base=b",
          "beforeOracleB:num=2,base=b",
          "givenOracleB:true",
          "whenOracleB:HELLO",
          "thenOracleB:true",
          "afterOracleB:num=2,base=b",
          "beforeOracleB:num=10,base=b",
          "givenOracleB:true",
          "whenOracleB:HELLO",
          "thenOracleB:true",
          "afterOracleB:num=10,base=b",
          "tearDown:base=b",
          "tearDownAfterAll:null"
      )).$();
    }

    @Override
    public String getScriptName() {
      return "tests/issues/issue-37-groupByTestFixtureOrderingByTestOracle.json";
    }
  }

  public static class Memoization extends Base {
    @Override
    Matcher<? super Object> junitResultMatcher(AsObject<Object, Object> junitResultMatcherBuilder) {
      return junitResultMatcherBuilder
          .check(call("wasSuccessful").$(), isTrue())
          .check(call("getFailureCount").$(), equalTo(0))
          .$();    }

    @Override
    Matcher<? super Object> outputMatcher(AsList<Object, String> outputMatcherBuilder) {
      return asObject().any();
    }

    @Override
    public String getScriptName() {
      return "tests/issues/issue-37-memoization.json";
    }
  }
}