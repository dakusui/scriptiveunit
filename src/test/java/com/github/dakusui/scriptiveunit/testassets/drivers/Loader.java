package com.github.dakusui.scriptiveunit.testassets.drivers;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBasedTestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.HostSpec;
import com.github.dakusui.scriptiveunit.testutils.Resource;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Arrays;

import static java.util.stream.Collectors.toList;


public abstract class Loader extends JsonBasedTestSuiteDescriptorLoader {
  @SuppressWarnings("WeakerAccess")
  protected Loader(Config config) {
    super(config);
  }

  @Override
  protected ApplicationSpec createApplicationSpec() {
    return new ApplicationSpec.Standard() {
      HostSpec.Json hostSpec = new HostSpec.Json();

      @Override
      public Dictionary createDefaultValues() {
        Dictionary work = super.createDefaultValues();
        for (ObjectNode each : objectNodes())
          work = this.deepMerge(hostSpec.toApplicationDictionary(each), work);
        return this.removeInheritanceDirective(work);
      }
    };
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
