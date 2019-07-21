package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.libs.Core;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.Strings;
import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.junit.runner.RunWith;

@RunWith(ScriptiveUnit.class)
public abstract class SimpleTestBase {

  abstract static class Compiler extends ScriptCompiler.Default implements ApplicationSpec.Dictionary.Factory {
    Compiler() {
    }
  }

  @Import
  public Object core       = new Core();
  @Import
  public Object predicates = new Predicates();
  @Import
  public Object strings    = new Strings();

  @Import
  public Object broken = new Broken();

  public static class Broken {
    @Scriptable
    public Value<String> brokenForm() {
      throw new RuntimeException("brokenForm");
    }
  }
}
