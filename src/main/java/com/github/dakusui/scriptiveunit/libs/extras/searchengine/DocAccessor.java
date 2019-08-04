package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.Optional;

public interface DocAccessor<DOC> {
  String idOf(DOC doc);

  Optional<?> valueOf(DOC doc, String fieldName);
}
