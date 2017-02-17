package com.github.dakusui.scriptunit.testutils.drivers;

import com.github.dakusui.scriptunit.core.Utils;
import com.github.dakusui.scriptunit.loaders.json.JsonBasedTestSuiteLoader;
import com.github.dakusui.scriptunit.testutils.Resource;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Arrays;

import static java.util.stream.Collectors.toList;


public abstract class Loader extends JsonBasedTestSuiteLoader {
  public Loader(Class<?> driverClass, String resourceName) {
    super(driverClass, resourceName);
  }

  protected ObjectNode readObjectNodeWithMerging(String resourceName) {
    ObjectNode work = super.readObjectNodeWithMerging(resourceName);
    for (ObjectNode each : objectNodes()) {
      work = Utils.deepMerge(each, work);
    }
    return work;
  }

  protected abstract ObjectNode[] objectNodes();

  public static Loader create(Class<?> driverClass, String rootResourceName, Resource<ObjectNode>... resources) {
    return new Loader(driverClass, rootResourceName) {
      @Override
      protected ObjectNode[] objectNodes() {
        return Arrays.stream(resources).map(Resource::get).collect(toList()).toArray(new ObjectNode[resources.length]);
      }
    };
  }
}
