package com.github.dakusui.scriptiveunit.unittests.core;

import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.scriptiveunit.unittests.core.UtJsonUtils.mergeObjectNodes;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ActionValueHandleTestOracleActionFactoryUtilsTest {
  @Test
  public void mergeTest() {
    ObjectNode a = JsonNodeFactory.instance.objectNode();
    a.put("a", "A");
    ObjectNode b = JsonNodeFactory.instance.objectNode();
    b.put("b", "B");

    assertThat(
        mergeObjectNodes(a, b),
        allOf(
            asString(call("get", "a").andThen("asText").$()).equalTo("A").$(),
            asString(call("get", "b").andThen("asText").$()).equalTo("B").$()
        ));
  }

  @Test
  public void whenAllTypesAnnotatedWith$thenThisClassIsFound() {
    assertTrue(
        ReflectionUtils.allTypesAnnotatedWith("com.github.dakusui.scriptiveunit", RunWith.class)
            .anyMatch(ActionValueHandleTestOracleActionFactoryUtilsTest.class::equals)
    );
  }

  @Test
  public void whenAllTypesAnnotatedWith$thenDummyClassIsNotFound() {
    assertTrue(
        ReflectionUtils.allTypesAnnotatedWith("com.github.dakusui.scriptiveunit", RunWith.class)
            .noneMatch(DummyClassNotAnnotatedWithRunWith.class::equals)
    );
  }

  @Test
  public void whenAllTypesUnderNonExistingPackageAnnotatedWith() {
    assertTrue(
        ReflectionUtils.allTypesAnnotatedWith("com.github.dakusui.non.existing", RunWith.class)
            .noneMatch(ActionValueHandleTestOracleActionFactoryUtilsTest.class::equals)
    );
  }

  @Test
  public void whenTryToFindExistingTestScript$thenFound() {
    assertTrue(ReflectionUtils.allScriptsUnder("tests").anyMatch((String s) -> s.equals("tests/testbase.json")));
  }

  @Test
  public void whenTryToFindNonExistingTestScript$thenNotFound() {
    assertTrue(ReflectionUtils.allScriptsUnder("tests").noneMatch((String s) -> s.equals("tests/non-existing.json")));
  }
}
