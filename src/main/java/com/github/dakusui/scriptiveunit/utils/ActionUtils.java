package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;

public enum ActionUtils {
  ;

  public static void performActionWithLogging(Action action) {
    ReportingActionPerformer performer = ReportingActionPerformer.create();
    performer.performAndReport(action, Writer.Slf4J.TRACE);
  }
}
