package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;

public enum ActionUtils {
  ;

  public static void performActionWithLogging(Action action) {
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter(ActionPrinter.Writer.Slf4J.TRACE));
    }
  }
}
