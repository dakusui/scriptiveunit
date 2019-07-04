package com.github.dakusui.scriptiveunit.testassets.drivers;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBasedTestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.testutils.Resource;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Arrays;

import static java.util.stream.Collectors.toList;


public abstract class Loader extends JsonBasedTestSuiteDescriptorLoader {
  private ApplicationSpec applicationSpec = new ApplicationSpec.Standard();

  @SuppressWarnings("WeakerAccess")
  protected Loader(Config config) {
    super(config);
  }

  protected ApplicationSpec.Dictionary readApplicationDictionaryWithMerging(String resourceName) {
    ApplicationSpec.Dictionary work = super.readApplicationDictionaryWithMerging(resourceName);
    for (ObjectNode each : objectNodes()) {
      work = applicationSpec.deepMerge(hostSpec.toApplicationDictionary(each), work);
    }
    return work;
  }

  protected abstract ObjectNode[] objectNodes();

  @SafeVarargs
  public static Loader create(Config config, Resource<ObjectNode>... resources) {
    return new Loader(config) {
      @Override
      protected ObjectNode[] objectNodes() {
        return Arrays.stream(resources).map(Resource::get).collect(toList()).toArray(new ObjectNode[resources.length]);
      }
    };
  }
}
