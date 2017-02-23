package com.github.dakusui.scriptiveunit.tests.variation;

import com.github.dakusui.jcunit.coverage.CombinatorialMetrics;
import com.github.dakusui.jcunit.plugins.caengines.IpoGcCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerImpl;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.testutils.Resource;
import com.github.dakusui.scriptiveunit.testutils.ResourceLevelsProvider;
import com.github.dakusui.scriptiveunit.testutils.drivers.Loader;
import com.github.dakusui.scriptiveunit.testutils.drivers.Simple;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;

import java.util.Properties;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(
    engine = @Generator(value = IpoGcCoveringArrayEngine.class, args = { @Value("2") }),
    checker = @Checker(value = SmartConstraintCheckerImpl.class),
    reporters = {
        @Reporter(value = CombinatorialMetrics.class, args = { @Value("2") })
    })
public class VariationTest {

  @ReflectivelyReferenced
  @FactorField(
      levelsProvider = ResourceLevelsProvider.FromJson.class,
      args = { @Value("components/_extends"), @Value("json") }
  )
  public Resource<ObjectNode> _extends;

  @ReflectivelyReferenced
  @FactorField(
      levelsProvider = ResourceLevelsProvider.FromJson.class,
      args = { @Value("components/description"), @Value("json") }
  )
  public Resource<ObjectNode> description;

  @ReflectivelyReferenced
  @FactorField(
      levelsProvider = ResourceLevelsProvider.FromJson.class,
      args = { @Value("components/factorSpace/factors"), @Value("json") }
  )
  public Resource<ObjectNode> factors;

  @ReflectivelyReferenced
  @FactorField(
      levelsProvider = ResourceLevelsProvider.FromJson.class,
      args = { @Value("components/factorSpace/constraints"), @Value("json") }
  )
  public Resource<ObjectNode> constraints;

  @ReflectivelyReferenced
  @FactorField(
      levelsProvider = ResourceLevelsProvider.FromJson.class,
      args = { @Value("components/runnerType"), @Value("json") }
  )
  public Resource<ObjectNode> runnerType;

  @ReflectivelyReferenced
  @FactorField(
      levelsProvider = ResourceLevelsProvider.FromJson.class,
      args = { @Value("components/setUp"), @Value("json") }
  )
  public Resource<ObjectNode> setUp;

  @ReflectivelyReferenced
  @FactorField(
      levelsProvider = ResourceLevelsProvider.FromJson.class,
      args = { @Value("components/setUpBeforeAll"), @Value("json") }
  )
  public Resource<ObjectNode> setUpBeforeAll;

  @ReflectivelyReferenced
  @FactorField(
      levelsProvider = ResourceLevelsProvider.FromJson.class,
      args = { @Value("components/testOracles"), @Value("json") }
  )
  public Resource<ObjectNode> testOracles;

  @Before
  public void before() {
    System.out.println(TestCaseUtils.toTestCase(this));
  }

  @Given({"isFactorsAttributePresent", "!isConstraintsAttributePresent"})
  @Test
  public void whenRunTest$thenTerminatesNormally() throws Throwable {
    runTest();
  }

  @Given("!isFactorsAttributePresent&&isConstraintsAttributePresent")
  @Test(expected = RuntimeException.class)
  public void whenRunTest$thenTerminatesWithMessage() throws Throwable {
    try {
      runTest();
    } catch (RuntimeException e) {
      assertThat(
          e.getMessage(),
          containsString(format(
              "Undefined factor(s) [sortBy, order] are used by (constraint)[sortBy, order]",
              Scriptable.class.getSimpleName(),
              Simple.class.getCanonicalName()
          )));
      throw e;
    }
  }

  @Condition
  public boolean isFactorsAttributePresent() {
    return this.factors.getName().endsWith("/valid-present.json");
  }

  @Condition
  public boolean isConstraintsAttributePresent() {
    return this.constraints.getName().endsWith("/valid-present.json");
  }

  private void runTest() throws Throwable {
    //noinspection unchecked
    new JUnitCore().run(
        new ScriptiveUnit(
            Simple.class,
            Loader.create(
                new Config.Builder(Simple.class, new Properties()).withScriptResourceName("components/root.json").build(),
                _extends,
                description,
                factors,
                constraints,
                runnerType,
                setUp,
                setUpBeforeAll,
                testOracles
            )));
  }
}
