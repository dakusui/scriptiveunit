package com.github.dakusui.scriptiveunit.tests.cli.compat;

import com.github.dakusui.jcunit.plugins.caengines.Ipo2CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerImpl;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.scriptiveunit.tests.cli.compat.factors.DriverLevel;
import com.github.dakusui.scriptiveunit.tests.cli.compat.factors.TargetTypeLevel;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(
    engine = @Generator(Ipo2CoveringArrayEngine.class),
    checker = @Checker(SmartConstraintCheckerImpl.class))
public class CliTest extends TestBase {

  @FactorField(stringLevels = { "list", "describe", "run" })
  public String subcommand;

  @FactorField(stringLevels = { "SUITESET", "SIMPLE" })
  public String driver;

  @FactorField(stringLevels = { "FUNCTION", "TESTCLASS", "SUITESET", "SCRIPT", "RUNNER" })
  public String targetType;

  @FactorField(intLevels = { -1, 0, 1, 2, 3 })
  public int targetNameIndex;


  @Uses("targetNameIndex")
  @Condition(constraint = true)
  public boolean validLevelIndexForTargetName() {
    return targetNameIndex != -1;
  }

  @Uses({ "targetNameIndex", "targetType" })
  @Condition(constraint = true)
  public boolean validTarget() {
    return targetNameIndex != -1 && targetNameIndex < targetType().numValidLevels();
  }

  private DriverLevel driver() {
    return DriverLevel.valueOf(this.driver);
  }

  private TargetTypeLevel targetType() {
    return TargetTypeLevel.valueOf(this.targetType);
  }

  private String targetName() {
    return TargetTypeLevel.valueOf(this.targetType).getTargetName(this);
  }

  @Uses({ "targetNameIndex", "targetType" })
  @Condition(constraint = true)
  public boolean forInvalidTargetTypeIndexShouldBeVoid() {
    //noinspection SimplifiableIfStatement
    if (targetType().numValidLevels() == 0)
      return targetNameIndex == -1;
    return true;
  }

  @Test
  @Given("validTarget")
  public void printCommandLine() {
    System.out.println(buildCommandLine());
  }

  @Test
  public void printTestCase() {
    System.out.println(TestCaseUtils.toTestCase(this));
  }

  private String buildCommandLine() {
    return "cli" + " " +
        this.subcommand + " " +
        this.targetType().name().toLowerCase() + " " +
        this.driver().getDriverClassName() + " " +
        this.targetName();
  }
}
