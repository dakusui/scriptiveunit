package com.github.dakusui.scriptiveunit.drivertests;

import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import org.junit.Test;

import static com.github.dakusui.scriptiveunit.testutils.UtUtils.createOracleStage;
import static com.github.dakusui.scriptiveunit.testutils.UtUtils.values;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;

public class PredicatesTest {
  private Predicates lib = new Predicates();

  private Stage stage = createOracleStage();

  @Test
  public void testOrTrue() {
    Object value = lib.or(values(FALSE, TRUE)).apply(stage);

    assertEquals(TRUE, value);
  }

  @Test
  public void testOrFalse() {
    Object value = lib.or(values(FALSE, FALSE)).apply(stage);

    assertEquals(FALSE, value);
  }
}
