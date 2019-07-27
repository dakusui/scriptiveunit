package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.utils.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

public class NodeFactoryTest {
  @Test
  public void test() {
    JsonNode node = new JsonUtils.NodeFactory<ObjectNode>() {
      @Override
      public JsonNode create() {
        return obj(
            $("key1", arr()),
            $("hello", $("world")));
      }
    }.create();

    System.out.println(node);
  }

  @Test
  public void test2() {
    JsonNode node = new JsonUtils.NodeFactory<ObjectNode>() {
      @Override
      public JsonNode create() {
        return obj(
            $("testOracles", arr()));
      }
    }.create();
    System.out.println(node);
  }

  @Test
  public void test3() {
    JsonNode node = new JsonUtils.NodeFactory<ObjectNode>() {
      @Override
      public JsonNode create() {
        return obj(
            $("testOracles", arr(
                obj(
                    $("when", arr("brokenForm")),
                    $("then", arr("matches", arr("output"), "bye"))
                ))));
      }
    }.create();
    System.out.println(node);
  }
}
