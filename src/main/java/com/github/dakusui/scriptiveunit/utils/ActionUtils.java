package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.scriptiveunit.model.session.action.TestActionBuilder;
import org.junit.AssumptionViolatedException;

public enum ActionUtils {
  ;

  public static void performActionWithLogging(Action action) {
    ReportingActionPerformer performer = ReportingActionPerformer.create();
      performer.performAndReport(action, Writer.Slf4J.TRACE);
  }

  public static String formatAction(Action action) {
    if (action instanceof Named)
      return ((Named) action).name();
    return action.toString();
  }

  public static <I, O> TestActionBuilder<I, O> test() {
    return new TestActionBuilder<>("hello");
  }
}
