package com.github.dakusui.scriptiveunit.examples;

import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.loaders.json.HostLanguage;
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

import static com.github.dakusui.scriptiveunit.loaders.json.JsonPreprocessorUtils.requireObjectNode;

@Load(with = DryQapi.Loader.class)
public class DryQapi extends Qapi {
  /**
   * A resource that holds default values of ScriptiveUnit.
   */
  protected static final String DEFAULTS_JSON = "defaults/values.json";

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
    protected ObjectNode readScriptHandlingInheritance(String resourceName) {
      System.out.println("<" + resourceName + ">");
      ObjectNode work = readObjectNodeDirectlyWithMerging(resourceName);
      ObjectNode ret = requireObjectNode(JsonUtils.readJsonNodeFromStream(ReflectionUtils.openResourceAsStream(DEFAULTS_JSON)));
      ret = JsonUtils.deepMerge(work, ret);
      ret.remove(HostLanguage.Json.EXTENDS_KEYWORD);
      return ret;
    }

    ObjectNode readObjectNodeDirectlyWithMerging(String script) {
      ObjectNode child = requireObjectNode(preprocess(requireObjectNode(JsonUtils.readJsonNodeFromStream(toInputStream(script))), getPreprocessors()));
      ObjectNode work = JsonNodeFactory.instance.objectNode();
      if (child.has(HostLanguage.Json.EXTENDS_KEYWORD)) {
        JsonPreprocessorUtils.getParentsOf(child, HostLanguage.Json.EXTENDS_KEYWORD).forEach(s -> JsonUtils.deepMerge(requireObjectNode(readObjectNodeWithMerging(s)), work));
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
