package com.github.dakusui.scriptiveunit.loaders.preprocessing;

import java.util.function.BiFunction;

@FunctionalInterface
public interface RawScriptReader<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE>
    extends BiFunction<String, HostSpec<NODE, OBJECT, ARRAY, ATOM>, ApplicationSpec.Dictionary> {
}
