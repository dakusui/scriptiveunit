package com.github.dakusui.scriptiveunit.examples;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.libs.Arith;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.unittests.cli.MemoizationExample;
import org.junit.runner.RunWith;

@Load(with = MemoizationDriverExample.Loader.class)
@RunWith(ScriptiveUnit.class)
public class MemoizationDriverExample {
  public static class Loader extends TestSuiteDescriptorLoader.Impl {
    public Loader(Script script) {
      super(script);
    }
  }

  @Import
  public final Object memo = new MemoizationExample();
  @Import
  public final Object predicates = new Predicates();
  @Import
  public final Object arith = new Arith();
}
