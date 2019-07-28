package com.github.dakusui.scriptiveunit.unittests.cli;

import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.Preprocessor;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;

public class FromFileTest {
  @Test
  public void test() {
    JsonScript script = new JsonScript.FromFile(Object.class, "src/test/resources/examples/from_file_with_inheritance.json");
    System.out.println(script.mainNode());

    ApplicationSpec.Dictionary dictionary = Preprocessor.create(
        script.languageSpec().hostSpec(),
        script.languageSpec().applicationSpec()).preprocess(
        script.languageSpec().hostSpec().toApplicationDictionary(script.mainNode()),
        script.languageSpec().resourceStoreSpec());

    System.out.println(script.languageSpec().hostSpec().toHostObject(dictionary));

    assertThat(
        dictionary,
        allOf(
            asString(call("valueOf", "keyInFile").andThen("get").$())
                .equalTo("valueInFile").$(),
            asString(call("valueOf", "keyInChild").andThen("get").$())
                .equalTo("valueInChild").$()
        )
    );
  }
}
