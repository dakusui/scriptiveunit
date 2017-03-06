package com.github.dakusui.scriptiveunit.tests.cli;

/**
 * cli {subcommand} {target type} {test class} {target name}
 *
 * subcommand - run, list, describe
 * target type - function, driver, suiteset, script, runner
 * test class - FQCN of driver class. Must be annotated with ScriptiveUnit or ScriptiveSuiteSet
 * target - script resource name, function name,  or driver class
 *
 * cli list function
 * - your.DriverClass                  -> Only 'built-in' functions will be listed
 * - your.DriverClass your/script.json
 *
 * cli list driver                     -> Lists classes annotated with @RunWith(ScriptiveUnit.class)
 * - (none)
 * cli list suiteset                   -> Lists classes annotated with @RunWith(ScriptiveSuiteSet.class)
 * - (none)
 *
 * cli list runner
 * - (none)                            -> Lists supported runners
 * cli list script
 * -  your.SuiteSetClass               -> Lists scripts run by specified SuiteSetClass.
 *
 * cli run driver     your.DriverClass       your/script.json
 * cli run suiteset   your.SuiteSetClass
 *
 * cli describe function   your/Script.json yourFunction
 * cli describe driver     your.DriverClass
 * cli describe your/script.json
 * cli describe suiteset   your.SuiteSetClass
 *
 * cli list (
 */
public class Cli {
  enum Subcommand {
    RUN,
    LIST,
    DESCRIBE
  }
}
