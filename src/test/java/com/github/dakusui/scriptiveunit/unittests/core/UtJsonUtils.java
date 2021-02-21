package com.github.dakusui.scriptiveunit.unittests.core;

import com.github.dakusui.scriptiveunit.exceptions.SyntaxException;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.utils.Checks;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import static com.github.dakusui.scriptiveunit.unittests.core.UtJsonUtils.UtExceptionUtils.mergeFailed;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public enum UtJsonUtils {
  ;

  /**
   * Merges two object nodes and merged object node will be returned.
   * If {@code a} and {@code b} has the same attributes the value comes from
   * {@code a} will override the other's.
   * Values in both {@code a} and {@code b} will not be changed and new object node
   * will be returned
   *
   * @param a An object node.
   * @param b An object node.
   * @return A merged object node that is newly created.
   */
  public static Object mergeObjectNodes(ObjectNode a, ObjectNode b) {
    return deepMerge(a, (ObjectNode) JsonNodeFactory.instance.objectNode().putAll(b));
  }

  /**
   * Merges {@code source} object node into {@code target} and returns {@code target}.
   *
   * @param source An object node to be merged into {@code target}.
   * @param target An object node to be put attributes in {@code source}
   * @return {@code target} object node
   */
  @SuppressWarnings("NullableProblems")
  public static ObjectNode deepMerge(ObjectNode source, ObjectNode target) {
    requireNonNull(source);
    requireNonNull(target);
    for (String key : (Iterable<String>) source::getFieldNames) {
      JsonNode sourceValue = source.get(key);
      if (!target.has(key)) {
        // new value for "key":
        target.put(key, sourceValue);
      } else {
        // existing value for "key" - recursively deep merge:
        if (sourceValue.isObject()) {
          ObjectNode sourceObject = (ObjectNode) sourceValue;
          JsonNode targetValue = target.get(key);
          Checks.check(targetValue.isObject(), () -> mergeFailed(source, target, key));
          deepMerge(sourceObject, (ObjectNode) targetValue);
        } else {
          target.put(key, sourceValue);
        }
      }
    }
    return target;
  }

  enum UtExceptionUtils {
    ;

    public static SyntaxException mergeFailed(ApplicationSpec.Dictionary source, ApplicationSpec.Dictionary target, String key) {
      throw new SyntaxException(format("Failed to merge '%s' and '%s' on '%s'", source, target, key));
    }

    public static SyntaxException mergeFailed(ObjectNode source, ObjectNode target, String key) {
      throw new SyntaxException(format("Failed to merge '%s' and '%s' on '%s'", source, target, key));
    }
  }
}
