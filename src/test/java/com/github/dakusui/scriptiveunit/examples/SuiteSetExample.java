package com.github.dakusui.scriptiveunit.examples;

import com.github.dakusui.scriptiveunit.utils.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.utils.ScriptiveSuiteSet.SuiteScripts;
import org.junit.runner.RunWith;

@RunWith(ScriptiveSuiteSet.class)
@SuiteScripts(
    driverClass = Qapi.class,
    prefix = "",
    includes = { /*".*api.json", ".*issue.*json",*/ ".*print_twice.json" },
    excludes = { ".*iss.*" })
public class SuiteSetExample {
}
