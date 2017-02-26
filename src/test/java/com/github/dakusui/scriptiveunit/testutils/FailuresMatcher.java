package com.github.dakusui.scriptiveunit.testutils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.runner.notification.Failure;

import java.util.List;

public abstract class FailuresMatcher extends BaseMatcher<List<Failure>> {
  public static FailuresMatcher DEFAULT = new FailuresMatcher() {
    @Override
    public void describeTo(Description description) {
      description.appendText("(this passes always)");
    }

    @Override
    protected EntryMatcher forEntryAt(int index) {
      return EntryMatcher.MATCHES_ALWAYS;
    }
  };

  @Override
  public boolean matches(Object item) {
    if (!(item instanceof List))
      return false;
    int i = 0;
    for (Object each : ((List) item)) {
      if (!(each instanceof Failure)) {
        return false;
      }
      if (!forEntryAt(i).matches(each))
        return false;
      i++;
    }
    return true;
  }

  protected abstract EntryMatcher forEntryAt(int index);

  public abstract static class EntryMatcher extends BaseMatcher<Failure> {
    static final EntryMatcher MATCHES_ALWAYS = new EntryMatcher() {
      @Override
      public void describeTo(Description description) {
        description.appendText("(this matcher matches always)");
      }

      @Override
      protected boolean matchesFailure(Failure failure) {
        return true;
      }
    };

    @Override
    public boolean matches(Object item) {
      if (item instanceof Failure)
        return this.matchesFailure((Failure) item);
      return false;
    }

    abstract protected boolean matchesFailure(Failure failure);
  }
}
