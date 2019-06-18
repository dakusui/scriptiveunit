package com.github.dakusui.scriptiveunit.examples;

import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.json.JsonPreprocessorUtils;
import com.github.dakusui.scriptiveunit.loaders.json.JsonUtils;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

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
      ObjectNode ret = JsonPreprocessorUtils.checkObjectNode(JsonUtils.readJsonNodeFromStream(ReflectionUtils.openResourceAsStream(DEFAULTS_JSON)));
      ret = JsonUtils.deepMerge(work, ret);
      ret.remove(EXTENDS_KEYWORD);
      return ret;
    }

    ObjectNode readObjectNodeDirectlyWithMerging(String script) {
      ObjectNode child = JsonPreprocessorUtils.checkObjectNode(preprocess(JsonUtils.readJsonNodeFromStream(toInputStream(script))));
      ObjectNode work = JsonNodeFactory.instance.objectNode();
      if (child.has(EXTENDS_KEYWORD)) {
        JsonPreprocessorUtils.getParentsOf(child, EXTENDS_KEYWORD).forEach(s -> JsonUtils.deepMerge(JsonPreprocessorUtils.checkObjectNode(readObjectNodeWithMerging(s)), work));
      }
      return JsonUtils.deepMerge(child, work);
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
