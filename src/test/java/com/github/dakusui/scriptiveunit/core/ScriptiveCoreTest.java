package com.github.dakusui.scriptiveunit.core;

import com.github.dakusui.scriptiveunit.ScriptiveCore;
import com.github.dakusui.scriptiveunit.exceptions.FacadeException;
import com.github.dakusui.scriptiveunit.testassets.Driver1;
import com.github.dakusui.scriptiveunit.testassets.Driver2;
import com.github.dakusui.scriptiveunit.testassets.SuiteSet1;
import com.github.dakusui.scriptiveunit.testassets.SuiteSet2;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class ScriptiveCoreTest {
  private static final String ASSET_PACKAGE = "com.github.dakusui.scriptiveunit.testassets";

  @Test
  public void whenListFunctions$thenListed() {
    assertEquals(
        asList("print_twice", "print"),
        new ScriptiveCore().listFunctions(Driver1.class, "tests/suiteset/suite1.json").stream()
            .sorted()
            .collect(toList())
    );
  }

  @Test
  public void whenListDrivers$thenListed() {
    assertEquals(
        asList(
            Driver1.class.getCanonicalName(),
            Driver2.class.getCanonicalName()
        ),
        new ScriptiveCore().listDrivers(ASSET_PACKAGE).stream()
            .map(Class::getCanonicalName)
            .sorted()
            .collect(toList())
    );
  }

  @Test
  public void whenListRunners$thenListed() {
    assertEquals(
        asList(
            "groupByTestCase",
            "groupByTestFixture",
            "groupByTestFixtureOrderByTestOracle",
            "groupByTestOracle"
        ),
        new ScriptiveCore().listRunners().stream().sorted().collect(toList())
    );
  }

  @Test
  public void whenListSuiteSets$thenListed() {
    assertEquals(
        asList(
            SuiteSet1.class.getCanonicalName(),
            SuiteSet2.class.getCanonicalName()
        ),
        new ScriptiveCore().listSuiteSets(ASSET_PACKAGE).stream()
            .map(Class::getCanonicalName)
            .sorted()
            .collect(toList())
    );
  }


  @Test
  public void whenListScripts$thenListed() {
    assertEquals(
        asList(
            "tests/suiteset/suite1.json",
            "tests/suiteset/suite2.json"
        ),
        new ScriptiveCore().listScripts(SuiteSet1.class).stream().sorted().collect(toList())
    );
  }

  @Test(expected = FacadeException.class)
  public void whenListScriptsOnInvalidSuiteSetClass$thenFacadeExceptionWillBeThrown() {
    new ScriptiveCore().listScripts(SuiteSet2.class);
  }
}
