package com.github.dakusui.scriptiveunit.unittests.variation;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Given;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.testassets.drivers.Loader;
import com.github.dakusui.scriptiveunit.testassets.drivers.Simple;
import com.github.dakusui.scriptiveunit.testutils.Resource;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.dakusui.scriptiveunit.core.Utils.allScriptsUnderMatching;


@RunWith(JCUnit8.class)
public class VariationTest {
  private Parameter.Factory<Resource<ObjectNode>> createResourceParameterFactory(String resourcePackagePrefix, String suffix) {
    List<String> resourceNames = allScriptsUnderMatching(
        resourcePackagePrefix,
        Pattern.compile(".+\\." + suffix + "$")
    ).collect(Collectors.toList());

    return Parameter.Simple.Factory.of(
        resourceNames.stream()
            .map((Function<String, Resource<ObjectNode>>) s -> new Resource.Base<ObjectNode>(s) {
              @Override
              protected ObjectNode readObjectFromStream(InputStream is) {
                return (ObjectNode) Utils.readJsonNodeFromStream(is);
              }
            })
            .collect(Collectors.toList())
    );
  }

  @ParameterSource
  public Parameter.Factory<Resource<ObjectNode>> _extends() {
    return createResourceParameterFactory("components/_extends", "json");
  }

  @ParameterSource
  public Parameter.Factory<Resource<ObjectNode>> description() {
    return createResourceParameterFactory("components/description", "json");
  }

  @ParameterSource
  public Parameter.Factory<Resource<ObjectNode>> factors() {
    return createResourceParameterFactory("components/factorSpace/factors", "json");
  }

  @ParameterSource
  public Parameter.Factory<Resource<ObjectNode>> constraints() {
    return createResourceParameterFactory("components/factorSpace/constraints", "json");
  }

  @ParameterSource
  public Parameter.Factory<Resource<ObjectNode>> runnerType() {
    return createResourceParameterFactory("components/runnerType", "json");
  }

  @ParameterSource
  public Parameter.Factory<Resource<ObjectNode>> setUp() {
    return createResourceParameterFactory("components/setUp", "json");
  }

  @ParameterSource
  public Parameter.Factory<Resource<ObjectNode>> setUpBeforeAll() {
    return createResourceParameterFactory("components/setUpBeforeAll", "json");
  }

  @ParameterSource
  public Parameter.Factory<Resource<ObjectNode>> testOracles() {
    return createResourceParameterFactory("components/testOracles", "json");
  }

  @Given({ "isFactorsAttributePresent", "!isConstraintsAttributePresent" })
  @Test
  public void whenRunTest$thenTerminatesNormally(
      @From("_extends") Resource<ObjectNode> _extends,
      @From("description") Resource<ObjectNode> description,
      @From("factors") Resource<ObjectNode> factors,
      @From("constraints") Resource<ObjectNode> constraints,
      @From("runnerType") Resource<ObjectNode> runnerType,
      @From("setUp") Resource<ObjectNode> setUp,
      @From("setUpBeforeAll") Resource<ObjectNode> setUpBeforeAll,
      @From("testOracles") Resource<ObjectNode> testOracles
  ) throws Throwable {
    runTest(
        _extends,
        description,
        factors,
        constraints,
        runnerType,
        setUp,
        setUpBeforeAll,
        testOracles
    );
  }

  @Given("!isFactorsAttributePresent&&isConstraintsAttributePresent")
  @Test(expected = RuntimeException.class)
  public void whenRunTest$thenTerminatesWithMessage(
      Resource<ObjectNode> _extends,
      Resource<ObjectNode> description,
      Resource<ObjectNode> factors,
      Resource<ObjectNode> constraints,
      Resource<ObjectNode> runnerType,
      Resource<ObjectNode> setUp,
      Resource<ObjectNode> setUpBeforeAll,
      Resource<ObjectNode> testOracles
  ) throws Throwable {
    runTest(
        _extends,
        description,
        factors,
        constraints,
        runnerType,
        setUp,
        setUpBeforeAll,
        testOracles
    );
  }

  @Condition
  public boolean isFactorsAttributePresent(
      @From("factors") Resource<ObjectNode> factors
  ) {
    return factors.getName().endsWith("/valid-present.json");
  }

  @Condition
  public boolean isConstraintsAttributePresent(
      @From("constraints") Resource<ObjectNode> constraints
  ) {
    return constraints.getName().endsWith("/valid-present.json");
  }

  private void runTest(
      Resource<ObjectNode> _extends,
      Resource<ObjectNode> description,
      Resource<ObjectNode> factors,
      Resource<ObjectNode> constraints,
      Resource<ObjectNode> runnerType,
      Resource<ObjectNode> setUp,
      Resource<ObjectNode> setUpBeforeAll,
      Resource<ObjectNode> testOracles
  ) throws Throwable {
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
