package com.github.dakusui.scriptiveunit.tests.cli;

import com.github.dakusui.jcunit.plugins.caengines.IpoGcCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerImpl;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.jcunit.runners.standard.rules.TestDescription;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static java.util.Arrays.asList;
import static org.junit.Assume.assumeTrue;


/**
 * cli {subcommand} {target type} {test class} {target name}
 * <p>
 * subcommand - run, list, describe
 * target type - function, driver, suiteset, script, runner
 * test class - FQCN of driver class. Must be annotated with ScriptiveUnit or ScriptiveSuiteSet
 * target - script resource name, function name,  or driver class
 * <p>
 * cli list function
 * - your.DriverClass                  -> Only 'built-in' functions will be listed
 * - your.DriverClass your/script.json
 * <p>
 * cli list driver                     -> Lists classes annotated with @RunWith(ScriptiveUnit.class)
 * - (none)
 * cli list suiteset                   -> Lists classes annotated with @RunWith(ScriptiveSuiteSet.class)
 * - (none)
 * <p>
 * cli list runner
 * - (none)                            -> Lists supported runners
 * cli list script
 * -  your.SuiteSetClass               -> Lists scripts run by specified SuiteSetClass.
 * <p>
 * cli run driver     your.DriverClass       your/script.json
 * cli run suiteset   your.SuiteSetClass
 * <p>
 * cli describe function   your/Script.json yourFunction
 * cli describe driver     your.DriverClass
 * cli describe your/script.json
 * cli describe suiteset   your.SuiteSetClass
 * <p>
 * cli list (
 */
@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(
    engine = @Generator(value = IpoGcCoveringArrayEngine.class, args = @Value("2")),
    checker = @Checker(SmartConstraintCheckerImpl.NoNegativeTests.class))
public class CliTest {
  @Rule
  public TestDescription testDescription = new TestDescription();

  @FactorField(stringLevels = { "function", "driver", "suiteset", "runner", "script" })
  public String resourceType;

  @FactorField(stringLevels = { "list", "describe", "run" })
  public String subcommand;

  @FactorField(stringLevels = { "print", "'*'", "notExistingFunction", "NOT_REQUIRED" })
  public String function;

  @FactorField(stringLevels = {
      "com.github.dakusui.scriptiveunit.testutils.drivers.Qapi",
      "'*'", "not.existing.Driver", "NOT_REQUIRED"
  })
  public String driver;

  @FactorField(stringLevels = {
      "com.github.dakusui.scriptiveunit.testutils.drivers.SuiteSetExample",
      "'*'", "not.existing.SuiteSet", "NOT_REQUIRED"
  })
  public String suiteset;

  @FactorField(stringLevels = { "groupByTestFixture", "'*'", "notExistingRunner", "NOT_REQUIRED" })
  public String runner;

  @FactorField(stringLevels = { "tests/regular/qapi.json", "'*'", "not/existing/script.json", "NOT_REQUIRED" })
  public String script;

  @Uses({ "subcommand", "resourceType" })
  @Condition(constraint = true)
  public boolean isSupportedOperation() {
    return subcommand().doesSupport(resourceType());
  }

  @Uses({ "subcommand", "resourceType", "function" })
  @Condition(constraint = true)
  public boolean functionShouldBeNOT_REQUIREDiffNotUsed() {
    return attributeShouldBeNOT_REQUIREDiffNotUsed("function");
  }

  @Uses({ "subcommand", "resourceType", "driver" })
  @Condition(constraint = true)
  public boolean driverShouldBeNOT_REQUIREDiffNotUsed() {
    return attributeShouldBeNOT_REQUIREDiffNotUsed("driver");
  }

  @Uses({ "subcommand", "resourceType", "suiteset" })
  @Condition(constraint = true)
  public boolean suitesetShouldBeNOT_REQUIREDiffNotUsed() {
    return attributeShouldBeNOT_REQUIREDiffNotUsed("suiteset");
  }

  @Uses({ "subcommand", "resourceType", "runner" })
  @Condition(constraint = true)
  public boolean runnerShouldBeNOT_REQUIREDiffNotUsed() {
    return attributeShouldBeNOT_REQUIREDiffNotUsed("runner");
  }

  @Uses({ "subcommand", "resourceType", "script" })
  @Condition(constraint = true)
  public boolean scriptShouldBeNOT_REQUIREDiffNotUsed() {
    return attributeShouldBeNOT_REQUIREDiffNotUsed("script");
  }

  @Condition()
  public boolean areAllArgumentsValid() {
    for (String each : resourcesInUse()) {
      if (levelsOf(each).indexOf(getFieldValueOf(each)) != 0)
        return false;
    }
    return true;
  }

  @Test
  public void printCommandLine() {
    System.out.println(this.testDescription.getTestCase().getId() + ":" + formatCommand(subcommand(), resourceType()));
  }

  @Test
  @Given("areAllArgumentsValid")
  public void whenRunValid$thenSuccess() {
    System.err.println(this.testDescription.getTestCase().getId() + ":" + formatCommand(subcommand(), resourceType()));
  }

  @Test
  public void printTestCase() {
    System.out.println(this.testDescription.getTestCase().getId() + ":" + this.testDescription.getTestCase().getTuple());
  }

  private List<String> resourcesInUse() {
    return resourceTypes().stream().filter(this::isAttributeUsed).collect(Collectors.toList());
  }

  private List<String> levelsOf(String fieldName) {
    try {
      return asList(this.getClass().getField(fieldName).getAnnotation(FactorField.class).stringLevels());
    } catch (NoSuchFieldException e) {
      throw wrap(e);
    }
  }

  private boolean attributeShouldBeNOT_REQUIREDiffNotUsed(String attributeName) {
    //noinspection SimplifiableIfStatement
    if (!isSupportedOperation()) {
      return true;
    }
    //noinspection SimplifiableIfStatement
    if (isAttributeUsed(attributeName)) {
      return !"NOT_REQUIRED".equals(this.getFieldValueOf(attributeName));
    }
    return "NOT_REQUIRED".equals(this.getFieldValueOf(attributeName));
  }

  private boolean isAttributeUsed(String attributeName) {
    return getArgumentsFormat().contains(toTemplatingAttributeName(attributeName));
  }

  private ResourceType resourceType() {
    return ResourceType.fromString(resourceType);
  }

  private Subcommand subcommand() {
    return Subcommand.fromString(subcommand);
  }

  private String formatCommand(Subcommand subcommand, ResourceType resourceType) {
    return "scriptiveunit " + formatCommandLine(subcommand, resourceType);
  }

  private String doTemplatingOnArguments(String argumentFormat) {
    String ret = argumentFormat;
    for (String fieldName : resourceTypes()) {
      ret = ret.replace(toTemplatingAttributeName(fieldName), getFieldValueOf(fieldName));
    }
    return ret;
  }

  private List<String> resourceTypes() {
    return asList("function", "driver", "suiteset", "runner", "script");
  }

  private static String toTemplatingAttributeName(String fieldName) {
    return fieldName.toUpperCase();
  }

  private String getFieldValueOf(String fieldName) {
    try {
      return String.class.cast(this.getClass().getField(fieldName).get(this));
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw wrap(e);
    }
  }


  private String formatCommandLine(Subcommand subcommand, ResourceType resourceType) {
    assumeTrue(subcommand.doesSupport(resourceType));
    return String.format("%s %s %s", subcommand.asString(), resourceType.asString(), doTemplatingOnArguments(getArgumentsFormat()));
  }

  private String getArgumentsFormat() {
    return subcommand().composeArgumentsFormat(resourceType());
  }

  @SuppressWarnings("unused")
  enum ResourceType {
    FUNCTION(false) {
      @Override
      String formatListSubcommandArguments() {
        return "DRIVER SCRIPT";
      }

      @Override
      String formatDescribeSubcommandArguments() {
        return "DRIVER SCRIPT FUNCTION";
      }
    },
    DRIVER(false) {
      @Override
      String formatListSubcommandArguments() {
        return "";
      }

      @Override
      String formatDescribeSubcommandArguments() {
        return "DRIVER";
      }
    },
    SUITESET(true) {
      @Override
      String formatListSubcommandArguments() {
        return "";
      }

      @Override
      String formatDescribeSubcommandArguments() {
        return "SUITESET";
      }
    },
    RUNNER(false) {
      @Override
      String formatListSubcommandArguments() {
        return "";
      }

      @Override
      String formatDescribeSubcommandArguments() {
        return "RUNNER";
      }
    },
    SCRIPT(true) {
      @Override
      String formatListSubcommandArguments() {
        return "SUITESET";
      }

      @Override
      String formatDescribeSubcommandArguments() {
        return "DRIVER SCRIPT";
      }
    };

    final private boolean supportsList;
    final private boolean supportsDescribe;
    final private boolean supportsRun;

    ResourceType(boolean supportsRun) {
      this.supportsList = true;
      this.supportsDescribe = true;
      this.supportsRun = supportsRun;
    }

    static ResourceType fromString(String s) {
      return valueOf(s.toUpperCase());
    }

    String asString() {
      return this.name().toLowerCase();
    }

    boolean supportsList() {
      return this.supportsList;
    }

    boolean supportsDescribe() {
      return this.supportsDescribe;
    }

    boolean supportsRun() {
      return this.supportsRun;
    }

    abstract String formatListSubcommandArguments();

    abstract String formatDescribeSubcommandArguments();

    String formatRunSubcommandArguments() {
      return formatDescribeSubcommandArguments();
    }
  }

  @SuppressWarnings("unused")
  enum Subcommand {
    LIST {
      @Override
      boolean doesSupport(ResourceType resourceType) {
        return resourceType.supportsList();
      }

      @Override
      protected String composeArgumentsFormat(ResourceType resourceType) {
        return resourceType.formatListSubcommandArguments();
      }
    },
    DESCRIBE {
      @Override
      boolean doesSupport(ResourceType resourceType) {
        return resourceType.supportsDescribe();
      }

      @Override
      protected String composeArgumentsFormat(ResourceType resourceType) {
        return resourceType.formatDescribeSubcommandArguments();
      }
    },
    RUN {
      @Override
      boolean doesSupport(ResourceType resourceType) {
        return resourceType.supportsRun();
      }

      @Override
      protected String composeArgumentsFormat(ResourceType resourceType) {
        return resourceType.formatRunSubcommandArguments();
      }
    };

    static Subcommand fromString(String s) {
      return valueOf(s.toUpperCase());
    }

    public String asString() {
      return this.name().toLowerCase();
    }

    abstract boolean doesSupport(ResourceType resourceType);

    abstract protected String composeArgumentsFormat(ResourceType resourceType);
  }
}
