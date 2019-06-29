package com.github.dakusui.scriptiveunit.testassets;


import com.github.dakusui.scriptiveunit.runners.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.runners.ScriptiveSuiteSet.SuiteScripts;
import org.junit.runner.RunWith;

@RunWith(ScriptiveSuiteSet.class)
@SuiteScripts(driverClass = Driver1.class, includes = { "tests/suiteset/suite.\\.json" })
public class SuiteSet1 {
}
