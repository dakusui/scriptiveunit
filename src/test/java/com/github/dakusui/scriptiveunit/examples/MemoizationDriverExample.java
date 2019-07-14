package com.github.dakusui.scriptiveunit.examples;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.libs.Arith;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBasedTestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.unittests.cli.MemoizationExample;
import org.junit.runner.RunWith;

@Load(with = MemoizationDriverExample.Loader.class)
@RunWith(ScriptiveUnit.class)
public class MemoizationDriverExample {
  public static class Loader extends JsonBasedTestSuiteDescriptorLoader {
    public Loader(Config config) {
      super(config);
    }
  }

  @Import
  public final Object memo = new MemoizationExample();
  @Import
  public final Object predicates = new Predicates();
  @Import
  public final Object   arith = new Arith();
}
