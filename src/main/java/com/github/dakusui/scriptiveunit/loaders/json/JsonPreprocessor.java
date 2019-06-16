package com.github.dakusui.scriptiveunit.loaders.json;

import com.github.dakusui.scriptiveunit.loaders.Preprocessor;
import org.codehaus.jackson.JsonNode;

import java.util.function.Function;
import java.util.function.Predicate;

public interface JsonPreprocessor extends Preprocessor<JsonNode> {
  /**
   * Returns a preprocessor instance which translates a JSON node ona path specified by {@code pathMatcher}
   * in a given {@code JSON} node using a function {@code translator}.
   *
   * @param translator  A function with which the translation is made.
   * @param pathMatcher A predicate that returns {@code true} for a path in a JSON node,
   *                    where translations by {@code translator} are desired.
   * @return A new preprocessor.
   */
  static JsonPreprocessor preprocessor(Function<JsonNode, JsonNode> translator, Predicate<Path> pathMatcher) {
    return new JsonPreprocessor() {
      Preprocessor<JsonNode> inner = Preprocessor.preprocessor(translator, pathMatcher);

      @Override
      public JsonNode translate(JsonNode targetElement) {
        return inner.translate(targetElement);
      }

      @Override
      public boolean matches(Path pathToTargetElement) {
        return inner.matches(pathToTargetElement);
      }
    };
  }
}
