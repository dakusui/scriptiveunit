package com.github.dakusui.scriptiveunit.drivertests;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.scriptiveunit.libs.extras.Reporting;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.scriptiveunit.testutils.UtUtils.createForm;
import static com.github.dakusui.scriptiveunit.testutils.UtUtils.createOracleLevelStage;
import static com.github.dakusui.scriptiveunit.utils.ActionUtils.performActionWithLogging;

public class ReportingTest {
  private final Stage stage = createOracleLevelStage();
  private final Reporting reporting = new Reporting();

  @Test
  public void test_write_report() {
    assertThat(
        reporting.write_report(createForm("hello"), createForm("world")).apply(stage),
        allOf(
            asObject().isInstanceOf(String.class).$(),
            asString("toString").equalTo("world").$()
        ));
  }

  @Test
  public void test_put() {
    assertThat(
        reporting.put(createForm("hello"), createForm("world")).apply(stage),
        allOf(
            asObject().isInstanceOf(String.class).$(),
            asString("toString").equalTo("world").$()
        ));
  }

  @Test
  public void test_get() {
    reporting.put(createForm("hello"), createForm("world")).apply(stage);
    assertThat(
        reporting.get(createForm("hello")).apply(stage),
        allOf(
            asObject().isInstanceOf(String.class).$(),
            asString("toString").equalTo("world").$()
        ));
  }


  @Test
  public void test_submit() {
    reporting.put(createForm("hello"), createForm("world1")).apply(stage);
    Action action = reporting.submit().apply(stage);
    assertThat(
        action,
        asObject().isInstanceOf(Action.class).$()
    );
  }

  @Test
  public void test_submit_perform() {
    reporting.put(createForm("hello"), createForm("world2")).apply(stage);
    Action action = reporting.submit().apply(stage);
    performActionWithLogging(action);
  }
}
