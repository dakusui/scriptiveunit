package com.github.dakusui.scriptiveunit.drivertests;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.scriptiveunit.drivers.actions.Basic;
import com.github.dakusui.scriptiveunit.model.form.FormList;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.testutils.UtUtils;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicTest {
  private final Stage stage = UtUtils.createOracleLevelStage();
  private final Basic basic = new Basic();

  @Test
  public void testPrint() {
    perform(basic.print(s -> "HELLO").apply(stage));
  }

  @Test(expected = RuntimeException.class)
  public void testFail() {
    try {
      perform(basic.fail(UtUtils.createForm("helloFail")).apply(stage));
    } catch (RuntimeException e) {
      assertEquals("helloFail", e.getMessage());
      throw e;
    }
  }

  @Test
  public void testPerformTrue() {
      assertTrue(basic.perform(FormList.create(asList(
          basic.print(UtUtils.createForm("helloPrint")))))
          .apply(stage));
  }

  @Test(expected = RuntimeException.class)
  public void testPerformFalse() {
    try {
      basic.perform(FormList.create(asList(
          basic.print(UtUtils.createForm("helloPrint")),
          basic.fail(UtUtils.createForm("helloFail")))))
          .apply(stage);
    } catch (RuntimeException e) {
      assertEquals("helloFail", e.getMessage());
      throw e;
    }
  }

  private static void perform(Action action) {
    ReportingActionPerformer.create().performAndReport(
        action,
        Writer.Std.OUT
    );
  }
}
