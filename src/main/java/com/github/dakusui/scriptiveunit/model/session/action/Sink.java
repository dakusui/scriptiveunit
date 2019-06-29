package com.github.dakusui.scriptiveunit.model.session.action;

import com.github.dakusui.actionunit.core.Context;

import java.util.function.BiConsumer;

public interface Sink<T> extends BiConsumer<T, Context> {
}
