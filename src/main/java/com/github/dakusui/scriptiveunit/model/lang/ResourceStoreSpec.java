package com.github.dakusui.scriptiveunit.model.lang;

import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import org.codehaus.jackson.node.ObjectNode;

import java.io.InputStream;

import static com.github.dakusui.scriptiveunit.utils.JsonUtils.readJsonNodeFromStream;
import static com.github.dakusui.scriptiveunit.utils.JsonUtils.requireObjectNode;

/**
 * An interface that represents a "resource store", from which scripts are read.
 */
public interface ResourceStoreSpec {

  ObjectNode readObjectNode(String resourceName);

  abstract class Base implements ResourceStoreSpec {
    @Override
    public ObjectNode readObjectNode(String resourceName) {
      return requireObjectNode(readJsonNodeFromStream(this.openResource(resourceName)));
    }

    InputStream openResource(String resourceName) {
      return ReflectionUtils.openResourceAsStream(resourceName);
    }
  }

  class Impl extends Base {
    public Impl() {
    }
  }
}
