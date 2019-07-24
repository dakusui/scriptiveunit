package com.github.dakusui.scriptiveunit.testassets.drivers;

import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.testutils.Resource;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Arrays;

import static java.util.stream.Collectors.toList;


public abstract class Loader {
  private final ApplicationSpec applicationSpec;

  @SuppressWarnings("WeakerAccess")
  protected Loader(ApplicationSpec applicationSpec) {
    this.applicationSpec = applicationSpec;
  }

  public ApplicationSpec.Dictionary createDefaultValues() {
    HostSpec.Json hostSpec = new HostSpec.Json();
    ApplicationSpec.Dictionary work = applicationSpec.createDefaultValues();
    for (ObjectNode each : objectNodes())
      work = applicationSpec.deepMerge(hostSpec.toApplicationDictionary(each), work);
    return applicationSpec.removeInheritanceDirective(work);
  }

  protected abstract ObjectNode[] objectNodes();

  @SafeVarargs
  public static Loader create(ApplicationSpec applicationSpec, Resource<ObjectNode>... resources) {
    return new Loader(applicationSpec) {
      @Override
      protected ObjectNode[] objectNodes() {
        return Arrays.stream(resources).map(Resource::get).collect(toList()).toArray(new ObjectNode[resources.length]);
      }
    };
  }
}
