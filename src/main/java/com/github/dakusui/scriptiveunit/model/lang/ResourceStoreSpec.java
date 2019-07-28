package com.github.dakusui.scriptiveunit.model.lang;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.codehaus.jackson.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.github.dakusui.scriptiveunit.utils.IoUtils.openFile;
import static com.github.dakusui.scriptiveunit.utils.JsonUtils.readJsonNodeFromStream;
import static com.github.dakusui.scriptiveunit.utils.JsonUtils.requireObjectNode;
import static com.github.dakusui.scriptiveunit.utils.ReflectionUtils.openResourceAsStream;

/**
 * An interface that represents a "resource store", from which scripts are read.
 */
public interface ResourceStoreSpec {

  ObjectNode readObjectNode(String resourceName);

  abstract class Base implements ResourceStoreSpec {
    private final File baseDir;

    protected Base(File baseDir) {
      this.baseDir = baseDir;
    }

    @Override
    public ObjectNode readObjectNode(String resourceName) {
      try {
        try (InputStream i = open(resourceName)) {
          return requireObjectNode(readJsonNodeFromStream(i));
        }
      } catch (IOException e) {
        throw ScriptiveUnitException.wrapIfNecessary(e);
      }
    }

    InputStream open(String resourceName) {
      File file = new File(baseDir, resourceName);
      return file.exists() ?
          openFile(file) :
          openResourceAsStream(resourceName);
    }

  }

  class Impl extends Base {
    public Impl(File baseDir) {
      super(baseDir);
    }
  }
}
