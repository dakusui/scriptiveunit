package com.github.dakusui.scriptiveunit.model.session;


import com.github.dakusui.actionunit.core.Context;

import java.util.function.BiFunction;

public interface Pipe<I, O> extends BiFunction<I, Context, O> {
}
