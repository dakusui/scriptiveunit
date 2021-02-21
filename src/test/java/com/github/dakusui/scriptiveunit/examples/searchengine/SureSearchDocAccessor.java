package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.libs.extras.searchengine.DocAccessor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.Atom;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.Dictionary;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface SureSearchDocAccessor extends DocAccessor<Dictionary> {
  @Override
  default String idOf(Dictionary doc) {
    return requireNonNull(((Atom) doc.valueOf("id"))).get();
  }

  @Override
  default Optional<?> valueOf(Dictionary doc, String fieldName) {
    if (doc.containsKey(fieldName))
      return Optional.of(((Atom) doc.valueOf(fieldName)).get());
    return Optional.empty();
  }

  default String descriptionOf(Dictionary dictionary) {
    return (String) ((Atom) dictionary.valueOf("description")).get();
  }

}
