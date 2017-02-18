package com.github.dakusui.scriptiveunit.testutils;

import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.scriptiveunit.core.Utils;
import org.codehaus.jackson.node.ObjectNode;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.regex.Pattern;

public abstract class ResourceLevelsProvider<T> extends LevelsProvider.Base {
  private final LinkedList<String> resourceNames;

  public ResourceLevelsProvider(String resourcePackagePrefix, String suffix) {
    this.resourceNames = new LinkedList<>(
        new Reflections(
            resourcePackagePrefix,
            new ResourcesScanner()
        ).getResources(Pattern.compile(".+\\." + suffix + "$")));
  }

  @Override
  public int size() {
    return this.resourceNames.size();
  }

  @Override
  public Resource<T> get(int i) {
    return new Resource.Base<T>(this.resourceNames.get(i)) {
      @Override
      protected T readObjectFromStream(InputStream is) {
        return loadFromStream(is);
      }
    };
  }

  protected abstract T loadFromStream(InputStream is);


  public static class FromJson extends ResourceLevelsProvider<ObjectNode> {
    public FromJson(@Param(source = Param.Source.CONFIG) String resourcePackagePrefix, @Param(source = Param.Source.CONFIG) String suffix) {
      super(resourcePackagePrefix, suffix);
    }

    @Override
    protected ObjectNode loadFromStream(InputStream is) {
      return (ObjectNode) Utils.readJsonNodeFromStream(is);
    }
  }
}
