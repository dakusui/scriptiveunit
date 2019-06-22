package com.github.dakusui.scriptiveunit.unittests.core;

import com.github.dakusui.crest.Crest;
import com.github.dakusui.scriptiveunit.ScriptiveCore;
import com.github.dakusui.scriptiveunit.core.Description;
import com.github.dakusui.scriptiveunit.examples.Qapi;
import com.github.dakusui.scriptiveunit.exceptions.FacadeException;
import com.github.dakusui.scriptiveunit.exceptions.ResourceException;
import com.github.dakusui.scriptiveunit.testassets.Driver1;
import com.github.dakusui.scriptiveunit.testassets.Driver2;
import com.github.dakusui.scriptiveunit.testassets.SuiteSet1;
import com.github.dakusui.scriptiveunit.testassets.SuiteSet2;
import com.github.dakusui.scriptiveunit.testassets.drivers.ExampleSuiteSet;
import com.github.dakusui.scriptiveunit.testassets.drivers.Simple;
import com.github.dakusui.scriptiveunit.testutils.JUnitResultMatcher;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import com.github.dakusui.scriptiveunit.utils.ScriptiveSuiteSet;
import org.junit.Ignore;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.asListOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ScriptiveCoreTest extends TestBase {
  private static final String ASSET_PACKAGE = "com.github.dakusui.scriptiveunit.testassets";

  @Test
  public void whenDescribeFunction$thenDescribed() {
    Description description = new ScriptiveCore().describeFunction(Driver1.class, "tests/regular/driver1.json", "helloWorld");
    assertThat(description.content(), equalTo(asList("Hello, world", "everyone")));
    assertThat(description.children(), equalTo(emptyList()));
  }

  @Test
  public void whenDescribeUserDefinedFunction$thenDescribed() {
    Description description = new ScriptiveCore().describeFunction(Driver1.class, "tests/regular/driver1.json", "print_twice");
    assertThat(description.content(), equalTo(asList("(print", "  (print", "    (0)", "  )", ")")));
    assertThat(description.children(), equalTo(emptyList()));
  }

  @Test
  public void whenDescribeUserDefinedEmptyFunction$thenDescribed() {
    Description description = new ScriptiveCore().describeFunction(Driver1.class, "tests/regular/driver1.json", "empty");
    assertThat(description.content(), equalTo(singletonList("()")));
    assertThat(description.children(), equalTo(emptyList()));
  }

  @Test(expected = ResourceException.class)
  public void whenDescribeUndefinedEmptyFunction$thenDescribed() {
    new ScriptiveCore().describeFunction(Driver1.class, "tests/regular/driver1.json", "undefined");
  }

  @Test(expected = ResourceException.class)
  public void whenListFunctionsForDriver2$thenResourceExceptionThrown() {
    new ScriptiveCore().listFunctions(
        Driver1.class,
        "tests/regular/notExistingDriver.json"
    );
  }

  @Test
  public void whenListFunctions$thenListed() {
    Crest.assertThat(
        new ScriptiveCore().listFunctions(
            Driver1.class,
            "tests/regular/driver1.json")
            .stream()
            .sorted()
            .collect(toList()),
        asListOf(String.class)
            .containsAll(asList("empty", "helloWorld", "print", "print_twice"))
            .$());
  }

  @Test
  public void whenListDrivers$thenListed() {
    assertEquals(
        asList(
            Driver1.class.getCanonicalName(),
            Driver2.class.getCanonicalName(),
            Simple.class.getCanonicalName()
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
            SuiteSet2.class.getCanonicalName(),
            ExampleSuiteSet.class.getCanonicalName(),
            Simple.Run.class.getCanonicalName()
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

  /**
   * This test can only work when it is run individually since ScriptiveUnit
   * relies on a constant value created by referencing a system property.
   *
   * @see ScriptiveSuiteSet
   */
  @Ignore
  @Test
  public void given1outOf2partitionedExecutionDef_FirstOne$whenListScripts$thenListed() {
    System.setProperty(ScriptiveSuiteSet.SCRIPTIVEUNIT_PARTITION, "1:2");
    try {
      assertEquals(
          singletonList("tests/suiteset/suite2.json"),
          new ScriptiveCore().listScripts(SuiteSet1.class).stream().sorted().collect(toList())
      );
    } finally {
      System.getProperties().remove(ScriptiveSuiteSet.SCRIPTIVEUNIT_PARTITION);
    }
  }

  /**
   * This test can only work when it is run individually since ScriptiveUnit
   * relies on a constant value created by referencing a system property value.
   *
   * @see ScriptiveSuiteSet
   */
  @Ignore
  @Test
  public void given1outOf2partitionedExecutionDef_SecondOne$whenListScripts$thenListed() {
    System.setProperty(ScriptiveSuiteSet.SCRIPTIVEUNIT_PARTITION, "0:2");
    try {
      assertEquals(
          singletonList("tests/suiteset/suite1.json"),
          new ScriptiveCore().listScripts(SuiteSet1.class).stream().sorted().collect(toList())
      );
    } finally {
      System.getProperties().remove(ScriptiveSuiteSet.SCRIPTIVEUNIT_PARTITION);
    }
  }

  @Test(expected = FacadeException.class)
  public void whenListScriptsOnInvalidSuiteSetClass$thenFacadeExceptionWillBeThrown() {
    new ScriptiveCore().listScripts(SuiteSet2.class);
  }

  @Test
  public void whenRunScript$thenOK() {
    assertThat(
        new ScriptiveCore().runScript(Qapi.class, "tests/regular/print_twice.json"),
        new JUnitResultMatcher.Builder()
            .withExpectedResult(true)
            .withExpectedRunCount(2)
            .withExpectedIgnoreCount(0)
            .withExpectedFailureCount(0)
            .build()
    );
  }

  @Test
  public void whenRunSuiteSet$thenOK() {
    assertThat(
        new ScriptiveCore().runSuiteSet(ExampleSuiteSet.class),
        new JUnitResultMatcher.Builder()
            .withExpectedResult(false)
            .withExpectedRunCount(4)
            .withExpectedIgnoreCount(0)
            .withExpectedFailureCount(2)
            .build()
    );
  }
}
