package com.github.dakusui.scriptiveunit.examples;

import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Config;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import static com.github.dakusui.scriptiveunit.core.Utils.*;

@Load(with = DryQapi.Loader.class)
public class DryQapi extends Qapi {
  public static class Loader extends Qapi.Loader {
    public Loader(Config config) {
      super(config);
    }

    /**
     * This hacky implementation treats {@code resourceName} as the resource script itself rather
     * than its name.
     *
     * @param resourceName A string that contains the script itself to be run.
     */
    @Override
    protected ObjectNode readScript(String resourceName) {
      System.out.println("<" + resourceName + ">");
      ObjectNode work = readObjectNodeDirectlyWithMerging(resourceName);
      ObjectNode ret = checkObjectNode(readJsonNodeFromStream(openResourceAsStream(DEFAULTS_JSON)));
      ret = deepMerge(work, ret);
      ret.remove(EXTENDS_KEYWORD);
      return ret;
    }

    ObjectNode readObjectNodeDirectlyWithMerging(String script) {
      ObjectNode child = checkObjectNode(preprocess(readJsonNodeFromStream(toInputStream(script))));
      ObjectNode work = JsonNodeFactory.instance.objectNode();
      if (child.has(EXTENDS_KEYWORD)) {
        getParentsOf(child).forEach(s -> deepMerge(checkObjectNode(readObjectNodeWithMerging(s)), work));
      }
      return deepMerge(child, work);
    }

    private InputStream toInputStream(String script) {
      return new ByteArrayInputStream(script.getBytes(Charset.defaultCharset()));
    }
  }

  public static void main(String... args) {
    System.getProperties().put("scriptiveunit.target","{\"$extends\":[\"tests/issues/issue-28.json\"]}");
    Result result = JUnitCore.runClasses(DryQapi.class);
    System.out.println("wasSuccessful:" + result.wasSuccessful());
    for (Failure failure: result.getFailures()) {
      System.out.println(failure.getDescription().getMethodName());
    }
  }
}
