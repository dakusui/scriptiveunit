package com.github.dakusui.scriptiveunit.testutils;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.scriptiveunit.drivers.actions.Basic;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import org.junit.Test;

public class BasicTest {
  private final Stage stage = UtUtils.createOracleLevelStage();
  private final Basic basic = new Basic();

  @Test
  public void test() {
    test(basic.print(s -> "HELLO"));
  }

  public void test(Form<Action> form) {
    ReportingActionPerformer.create().performAndReport(
        form.apply(stage),
        Writer.Std.OUT
    );
  }

}
