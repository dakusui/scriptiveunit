package com.github.dakusui.scriptiveunit.modeltests;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.libs.Arith;
import com.github.dakusui.scriptiveunit.model.form.FormRegistry;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

public class StatementTest extends TestBase {
  @Test
  public void test() {
    Object atom = "hello";

    Statement.Factory statementFactory = new Statement.Factory(createFormRegistry(new Standard()), emptyMap());
    Statement statement = statementFactory.create(atom);
    Object value = statement.toValue().apply(createStage(new Standard()));
    System.out.println(value);
  }


  @Test
  public void test2() {
    Standard driverObject = new Standard();
    Statement.Factory statementFactory = new Statement.Factory(createFormRegistry(driverObject), emptyMap());

    Object atom = asList("add", 1, 2);
    Statement statement = statementFactory.create(atom);
    System.out.println(statement.toValue().apply(createStage(driverObject)));
  }

  private Stage createStage(Standard driverObject) {
    return Stage.Factory.frameworkStageFor(createConfig(driverObject), new Tuple.Impl());
  }

  private JsonScript createConfig(Standard driverObject) {
    return JsonScript.Default.create(FormRegistry.getFormRegistry(driverObject));
  }

  private FormRegistry createFormRegistry(Standard driverObject) {
    return FormRegistry.load(driverObject);
  }

  public static class Standard {
    @Import
    public Arith arith = new Arith();
  }
}
