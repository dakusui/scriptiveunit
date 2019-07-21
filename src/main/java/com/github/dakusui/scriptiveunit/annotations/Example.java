package com.github.dakusui.scriptiveunit.annotations;

import com.github.dakusui.scriptiveunit.core.ScriptLoader;

@CompileWith()
@LoadBy(
    value = ScriptLoader.class,
    args = @Value("")
)
public class Example {
}
