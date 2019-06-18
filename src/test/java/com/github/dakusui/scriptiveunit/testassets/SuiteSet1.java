package com.github.dakusui.scriptiveunit.testassets;


import com.github.dakusui.scriptiveunit.utils.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.utils.ScriptiveSuiteSet.SuiteScripts;
import org.junit.runner.RunWith;

@RunWith(ScriptiveSuiteSet.class)
@SuiteScripts(driverClass = Driver1.class, includes = { "tests/suiteset/suite.\\.json" })
public class SuiteSet1 {
}
