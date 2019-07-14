package com.github.dakusui.scriptiveunit.unittests.cli;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.util.List;

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
@Ignore
@RunWith(JCUnit8.class)
public class CliTest {
  @ParameterSource
  public Parameter.Simple.Factory<String> resourceType() {
    return Parameter.Simple.Factory.of(asList("function", "driver", "suiteset", "runner", "script"));
  }

  @ParameterSource
  public Parameter.Simple.Factory<String> subcommand() {
    return Parameter.Simple.Factory.of(asList("list", "describe", "run"));
  }

  @ParameterSource
  public Parameter.Simple.Factory<String> function() {
    return Parameter.Simple.Factory.of(asList("print", "'*'", "notExistingFunction", "NOT_REQUIRED"));
  }

  @ParameterSource
  public Parameter.Simple.Factory<String> driver() {
    return Parameter.Simple.Factory.of(asList("com.github.dakusui.scriptiveunit.testutils.libs.Qapi", "'*'", "not.existing.Driver", "NOT_REQUIRED"));
  }

  @ParameterSource
  public Parameter.Simple.Factory<String> suiteset() {
    return Parameter.Simple.Factory.of(asList("com.github.dakusui.scriptiveunit.testutils.libs.SuiteSetExample", "'*'", "not.existing.SuiteSet", "NOT_REQUIRED"));
  }

  @ParameterSource
  public Parameter.Simple.Factory<String> runner() {
    return Parameter.Simple.Factory.of(asList("groupByTestFixture", "'*'", "notExistingRunner", "NOT_REQUIRED"));
  }

  @ParameterSource
  public Parameter.Simple.Factory<String> script() {
    return Parameter.Simple.Factory.of(asList("examples/qapi.json", "'*'", "not/existing/script.json", "NOT_REQUIRED"));
  }

  @Condition(constraint = true)
  public boolean isSupportedOperation(
      @From("subcommand") String subcommand,
      @From("resourceType") String resourceType
  ) {
    return getSubcommand(subcommand).doesSupport(getResourceType(resourceType));
  }

  @Condition(constraint = true)
  public boolean functionShouldBeNOT_REQUIREDiffNotUsed(
      @From("subcommand") String subcommand,
      @From("resourceType") String resourceType,
      @From("function") String function
  ) {
    return attributeShouldBeNOT_REQUIREDiffNotUsed(
        subcommand,
        resourceType,
        "function",
        function
    );
  }

  @Condition(constraint = true)
  public boolean driverShouldBeNOT_REQUIREDiffNotUsed(
      @From("subcommand") String subcommand,
      @From("resourceType") String resourceType,
      @From("driver") String driver
  ) {
    // @Uses({ "subcommand", "resourceType", "driver" })
    return attributeShouldBeNOT_REQUIREDiffNotUsed(
        subcommand,
        resourceType,
        "driver",
        driver
    );
  }

  @Condition(constraint = true)
  public boolean suitesetShouldBeNOT_REQUIREDiffNotUsed(
      @From("subcommand") String subcommand,
      @From("resourceType") String resourceType,
      @From("suiteset") String suiteset
  ) {
    //@Uses({ "subcommand", "resourceType", "suiteset" })
    return attributeShouldBeNOT_REQUIREDiffNotUsed(
        subcommand,
        resourceType,
        "suiteset",
        suiteset
    );
  }

  @Condition(constraint = true)
  public boolean runnerShouldBeNOT_REQUIREDiffNotUsed(
      @From("subcommand") String subcommand,
      @From("resourceType") String resourceType,
      @From("runner") String runner
  ) {
    // @Uses({ "subcommand", "resourceType", "runner" })
    return attributeShouldBeNOT_REQUIREDiffNotUsed(
        subcommand,
        resourceType,
        "runner",
        runner
    );
  }

  @Condition(constraint = true)
  public boolean scriptShouldBeNOT_REQUIREDiffNotUsed(
      @From("subcommand") String subcommand,
      @From("resourceType") String resourceType,
      @From("script") String script
  ) {
    //@Uses({ "subcommand", "resourceType", "script" })
    return attributeShouldBeNOT_REQUIREDiffNotUsed(
        subcommand,
        resourceType,
        "script",
        script
    );
  }

  private boolean attributeShouldBeNOT_REQUIREDiffNotUsed(String subcommand, String resourceType, String targetAttributeName, String targetAttributeValue) {
    //noinspection SimplifiableIfStatement
    if (!isSupportedOperation(subcommand, resourceType)) {
      return true;
    }
    //noinspection SimplifiableIfStatement
    if (isAttributeUsed(subcommand, resourceType, targetAttributeName)) {
      return !"NOT_REQUIRED".equals(targetAttributeValue);
    }
    return "NOT_REQUIRED".equals(targetAttributeValue);
  }

  private boolean isAttributeUsed(String subcommand, String resourceType, String attributeName) {
    return getArgumentsFormat(subcommand, resourceType).contains(toTemplatingAttributeName(attributeName));
  }

  private ResourceType getResourceType(String resourceType) {
    return ResourceType.fromString(resourceType);
  }

  private Subcommand getSubcommand(String subcommand) {
    return Subcommand.fromString(subcommand);
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
      throw ScriptiveUnitException.wrapIfNecessary(e);
    }
  }


  private String formatCommandLine(Subcommand subcommand, ResourceType resourceType) {
    assumeTrue(subcommand.doesSupport(resourceType));
    return String.format("%s %s %s", subcommand.asString(), resourceType.asString(), doTemplatingOnArguments(getArgumentsFormat(subcommand.asString(), resourceType.asString())));
  }

  private String getArgumentsFormat(String subcommand, String resourceType) {
    return getSubcommand(subcommand).composeArgumentsFormat(getResourceType(resourceType));
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
