package com.github.dakusui.scriptiveunit.drivertests;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.scriptiveunit.libs.actions.Basic;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import com.github.dakusui.scriptiveunit.testutils.UtUtils;
import org.junit.Test;

import static com.github.dakusui.scriptiveunit.testutils.UtUtils.createOracleStage;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicTest extends TestBase {
  private final Stage stage = createOracleStage();
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
      assertTrue(basic.perform(ValueList.create(asList(
          basic.print(UtUtils.createForm("helloPrint")))))
          .apply(stage));
  }

  @Test(expected = RuntimeException.class)
  public void testPerformFalse() {
    try {
      basic.perform(ValueList.create(asList(
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
