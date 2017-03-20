package com.github.dakusui.scriptiveunit.testutils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.runner.Result;

import java.util.HashMap;
import java.util.Map;
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
        translate(Result::wasSuccessful, resultMatcher()),
        translate(Result::getRunCount, runCountMatcher()),
        translate(Result::getFailureCount, failureCountMatcher()),
        translate(Result::getIgnoreCount, ignoreCountMatcher()),
        translate(Result::getFailures, failuresMatcher())
    ).matches(item);
  }

  abstract protected Matcher<Boolean> resultMatcher();

  abstract protected Matcher<Integer> runCountMatcher();

  abstract protected Matcher<Integer> failureCountMatcher();

  abstract protected Matcher<Integer> ignoreCountMatcher();

  abstract protected FailuresMatcher failuresMatcher();

  private <T, U> Matcher<T> translate(Function<T, U> translator, Matcher<U> matcher) {
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
        .appendText("failures").appendDescriptionOf(failuresMatcher());
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
          .appendText("ignoreCountMatcher: ").appendValue(result.getIgnoreCount())
          .appendText("failuresMatcher: ").appendValue(result.getFailures());
    } else {
      description.appendText("was ").appendValue(item);
    }
  }


  public static class Impl extends JUnitResultMatcher {

    final private Matcher<Boolean> expectedResult;
    final private Matcher<Integer> expectedRunCount;
    final private Matcher<Integer> expectedFailureCount;
    final private Matcher<Integer> expectedIgnoreCount;
    private final FailuresMatcher  failuresMatcher;

    Impl(Matcher<Boolean> expectedResult, Matcher<Integer> expectedRunCount, Matcher<Integer> expectedFailureCount, Matcher<Integer> expectedIgnoreCount, FailuresMatcher failuresMatcher) {
      this.expectedResult = expectedResult;
      this.expectedRunCount = expectedRunCount;
      this.expectedFailureCount = expectedFailureCount;
      this.expectedIgnoreCount = expectedIgnoreCount;
      this.failuresMatcher = failuresMatcher;
    }

    @Override
    protected Matcher<Boolean> resultMatcher() {
      return this.expectedResult;
    }

    @Override
    protected Matcher<Integer> runCountMatcher() {
      return this.expectedRunCount;
    }

    @Override
    protected Matcher<Integer> failureCountMatcher() {
      return this.expectedFailureCount;
    }

    @Override
    protected Matcher<Integer> ignoreCountMatcher() {
      return this.expectedIgnoreCount;
    }

    @Override
    protected FailuresMatcher failuresMatcher() {
      return this.failuresMatcher;
    }
  }

  public static class Builder {
    private Matcher<Boolean> resultMatcher = equalTo(true);
    private Matcher<Integer> runCountMatcher = equalTo(0);
    private Matcher<Integer> ignoreCountMatcher = equalTo(0);
    private Matcher<Integer> failureCountMatcher = equalTo(0);
    private Map<Integer, FailuresMatcher.EntryMatcher> failureMatchers = new HashMap<>();

    public Builder() {
    }

    public Builder withExpectedResult(boolean expectedResult) {
      this.resultMatcher = equalTo(expectedResult);
      return this;
    }

    public Builder withExpectedRunCount(int expectedRunCount) {
      this.runCountMatcher = equalTo(expectedRunCount);
      return this;
    }

    public Builder withExpectedIgnoreCount(int expectedIgnoreCount) {
      this.ignoreCountMatcher = equalTo(expectedIgnoreCount);
      return this;
    }

    public Builder withExpectedFailureCount(int expectedFailureCount) {
      this.failureCountMatcher = equalTo(expectedFailureCount);
      return this;
    }

    public Builder addFailureMatcher(int index, FailuresMatcher.EntryMatcher failureMatcher) {
      this.failureMatchers.put(index, failureMatcher);
      return this;
    }

    public JUnitResultMatcher build() {
      FailuresMatcher failuresMatcher = new FailuresMatcher() {
        @Override
        public void describeTo(Description description) {
          description
              .appendText("Failure matchers: ")
              .appendValue(failureMatchers);
        }

        @Override
        protected EntryMatcher forEntryAt(int index) {
          return failureMatchers.containsKey(index) ?
              failureMatchers.get(index) :
              EntryMatcher.MATCHES_ALWAYS;
        }
      };
      return new Impl(
          this.resultMatcher,
          this.runCountMatcher,
          this.failureCountMatcher,
          this.ignoreCountMatcher,
          failuresMatcher
      );
    }
  }
}
