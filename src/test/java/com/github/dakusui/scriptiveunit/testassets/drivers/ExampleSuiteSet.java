package com.github.dakusui.scriptiveunit.testassets.drivers;


import com.github.dakusui.scriptiveunit.runners.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.runners.ScriptiveSuiteSet.SuiteScripts;
import com.github.dakusui.scriptiveunit.examples.Qapi;
import org.junit.runner.RunWith;

@RunWith(ScriptiveSuiteSet.class)
@SuiteScripts(
    driverClass = Qapi.class,
    includes = { "tests/suiteset/suite.\\.json" })
public class ExampleSuiteSet {
}
