package com.github.dakusui.scriptunit.testutils;

import com.github.dakusui.scriptunit.model.func.Func;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.runner.Result;

import java.util.function.Function;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;

public abstract class JUnitResultMatcher extends BaseMatcher<Result> {
  @Override
  public boolean matches(Object object) {
    if (!(object instanceof Result))
      return false;
    Result item = (Result) object;
    return allOf(
        translate((Func<Result, Boolean>) Result::wasSuccessful, resultMatcher()),
        translate((Func<Result, Integer>) Result::getRunCount, runCountMatcher()),
        translate((Func<Result, Integer>) Result::getFailureCount, failureCountMatcher()),
        translate((Func<Result, Integer>) Result::getIgnoreCount, ignoreCountMatcher())
    ).matches(item);
  }

  abstract protected Matcher<Boolean> resultMatcher();

  abstract protected Matcher<Integer> runCountMatcher();

  abstract protected Matcher<Integer> failureCountMatcher();

  abstract protected Matcher<Integer> ignoreCountMatcher();

  <T, U> Matcher<T> translate(Function<T, U> translator, Matcher<U> matcher) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(Object item) {
        //noinspection unchecked
        return matcher.matches(translator.apply((T) item));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(String.format("%s:%s", translator, matcher));
        matcher.describeTo(description);
      }
    };
  }

  @Override
  public void describeTo(Description description) {
    description
        .appendText("wasSuccessful: ").appendDescriptionOf(resultMatcher()).appendText(", ")
        .appendText("runCount: ").appendDescriptionOf(runCountMatcher()).appendText(", ")
        .appendText("failureCount: ").appendDescriptionOf(failureCountMatcher()).appendText(", and ")
        .appendText("ignoreCount: ").appendDescriptionOf(ignoreCountMatcher())
    ;
  }

  @Override
  public void describeMismatch(Object item, Description description) {
    if (item instanceof Result) {
      Result result = (Result) item;
      description
          .appendText("was ")
          .appendText("wasSuccessful: ").appendValue(result.wasSuccessful()).appendText(", ")
          .appendText("runCount: ").appendValue(result.getRunCount()).appendText(", ")
          .appendText("failureCount: ").appendValue(result.getFailureCount()).appendText(", and ")
          .appendText("ignoreCount: ").appendValue(result.getIgnoreCount())
      ;
    } else {
      description.appendText("was ").appendValue(item);
    }
  }


  public static class Impl extends JUnitResultMatcher {

    final private boolean expectedResult;
    final private int     expectedRunCount;
    final private int     expectedFailureCount;
    final private int     expectedIgnoreCount;

    public Impl(boolean expectedResult, int expectedRunCount, Integer expectedFailureCount, Integer expectedIgnoreCount) {
      this.expectedResult = expectedResult;
      this.expectedRunCount = expectedRunCount;
      this.expectedFailureCount = expectedFailureCount;
      this.expectedIgnoreCount = expectedIgnoreCount;
    }

    @Override
    protected Matcher<Boolean> resultMatcher() {
      return equalTo(this.expectedResult);
    }

    @Override
    protected Matcher<Integer> runCountMatcher() {
      return equalTo(this.expectedRunCount);
    }

    @Override
    protected Matcher<Integer> failureCountMatcher() {
      return equalTo(this.expectedFailureCount);
    }

    @Override
    protected Matcher<Integer> ignoreCountMatcher() {
      return equalTo(this.expectedIgnoreCount);
    }
  }
}
