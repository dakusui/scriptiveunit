package com.github.dakusui.scriptiveunit.modeltests;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.drivers.Arith;
import com.github.dakusui.scriptiveunit.model.form.handle.ObjectMethodRegistry;
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

    Statement.Factory statementFactory = new Statement.Factory(createEmptyObjectMethodRegistry(), emptyMap());
    Statement statement = statementFactory.create(atom);
    Object value = statement.toForm().apply(createStage());
    System.out.println(value);
  }


  @Test
  public void test2() {
    Object atom = asList("add", 1, 2);

    Statement.Factory statementFactory = new Statement.Factory(createEmptyObjectMethodRegistry(), emptyMap());
    Statement statement = statementFactory.create(atom);

    System.out.println(statement.toForm().apply(createStage()));
  }

  private Stage createStage() {
    return Stage.Factory.frameworkStageFor(createConfig(), new Tuple.Impl());
  }

  private Config createConfig() {
    return new Config.Default();
  }

  private ObjectMethodRegistry createEmptyObjectMethodRegistry() {
    return ObjectMethodRegistry.load(new Standard());
  }

  public static class Standard {
    @Import
    public Arith arith = new Arith();
  }
}
