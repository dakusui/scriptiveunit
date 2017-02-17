package com.github.dakusui.scriptunit.tests.cli;

import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerImpl;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.scriptunit.doc.Help;
import com.github.dakusui.scriptunit.drivers.Qapi;
import com.github.dakusui.scriptunit.exceptions.ScriptUnitException;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Objects;

@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(checker = @Checker(SmartConstraintCheckerImpl.class))
public class HelpTest {
  private static final String                NULL                = "(NULL)";
  private static final int                   NULL_FOR_SECOND_ARG = -1;
  private static final String                INVALID             = "(INVALID)";
  private static final int                   INVALID_SECOND_ARG  = 2;
  private static final Map<String, String[]> arg1Values          = new ImmutableMap.Builder<String, String[]>()
      .put("SCRIPT", new String[] { "tests/regular/qapi.json", "tests/regular/defaultValues.json", INVALID })
      .put("FUNCTION", new String[] { "always", "if_then", INVALID })
      .put("RUNNER", new String[] { "groupByTestOracle", "groupByTestCase", INVALID })
      .build();

  @FactorField(stringLevels = { "SCRIPT", "FUNCTION", "RUNNER", NULL })
  public String arg1;

  @FactorField(intLevels = { 0, 1, INVALID_SECOND_ARG, NULL_FOR_SECOND_ARG })
  public int arg2;

  @Condition(constraint = true)
  public boolean check() {
    //noinspection SimplifiableIfStatement
    if (Objects.equals(arg1, NULL)) {
      return Objects.equals(arg2, NULL_FOR_SECOND_ARG);
    }
    return true;
  }

  @Condition()
  public boolean argLengthIs0() {
    return argLength() == 0;
  }

  @Condition()
  public boolean argLengthIs1() {
    return argLength() == 1;
  }

  @Condition()
  public boolean argLengthIs2() {
    return argLength() == 2;
  }

  @Condition()
  public boolean secondParameterIsInvalid() {
    return argLength() == 2 && arg2 == INVALID_SECOND_ARG;
  }

  @Before
  public void before() {
    System.out.println(this);
  }

  @Given("argLengthIs0")
  @Test
  public void runHelp0() {
    Help.help(Qapi.class);
  }

  @Given("argLengthIs1")
  @Test
  public void runHelp1() {
    Help.help(Qapi.class, arg1);
  }

  @Given("argLengthIs2&&!secondParameterIsInvalid")
  @Test
  public void runHelp2() {
    Help.help(Qapi.class, arg1, arg1Values.get(arg1)[arg2]);
  }

  @Given("argLengthIs2&&secondParameterIsInvalid")
  @Test(expected = ScriptUnitException.class)
  public void runHelp2withInvalidSecondParameter() {
    Help.help(Qapi.class, arg1, arg1Values.get(arg1)[arg2]);
  }

  public String toString() {
    return String.format("%s:%s", arg1, arg2);
  }

  private int argLength() {
    if (Objects.equals(arg1, NULL))
      return 0;
    if (Objects.equals(arg2, NULL_FOR_SECOND_ARG))
      return 1;
    return 2;
  }
}
