package com.github.dakusui.scriptiveunit.unittests.preprocessing;

import com.github.dakusui.scriptiveunit.loaders.json.HostLanguage;
import com.github.dakusui.scriptiveunit.loaders.json.ModelSpec;
import com.github.dakusui.scriptiveunit.testutils.TestBase;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import static com.github.dakusui.scriptiveunit.loaders.json.ModelSpec.$;
import static com.github.dakusui.scriptiveunit.loaders.json.ModelSpec.array;
import static com.github.dakusui.scriptiveunit.loaders.json.ModelSpec.atom;
import static com.github.dakusui.scriptiveunit.loaders.json.ModelSpec.dict;
import static org.junit.Assert.assertEquals;

public class ModelSpecTest extends TestBase {
  @Test
  public void deepMergeTest1() {
    ModelSpec.Dictionary a = dict(
        $("a", atom("a")),
        $("a0", array(atom("a0"))),
        $("a1", dict($("a1", atom("a1")))),
        $("ab", dict($("aa", atom("aa")))));
    ModelSpec.Dictionary b = dict(
        $("ab", dict($("bb", atom("bb")))),
        $("b", atom("b")),
        $("b0", array(atom("b0"))),
        $("b1", dict($("b1", atom("b1"))))
    );

    ObjectNode objectNode = new HostLanguage.Json()
        .translate(ModelSpec.deepMerge(a, b));
    System.out.println(objectNode);
    assertEquals(
        "{\"ab\":{\"bb\":\"bb\",\"aa\":\"aa\"},\"b\":\"b\",\"b0\":[\"b0\"],\"b1\":{\"b1\":\"b1\"},\"a\":\"a\",\"a0\":[\"a0\"],\"a1\":{\"a1\":\"a1\"}}",
        objectNode.toString()
    );
  }
}
