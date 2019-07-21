package com.github.dakusui.scriptiveunit.examples;

import com.github.dakusui.scriptiveunit.annotations.CompatLoad;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.libs.Arith;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.unittests.cli.MemoizationExample;
import org.junit.runner.RunWith;

@CompatLoad(with = MemoizationDriverExample.Loader.class)
@RunWith(ScriptiveUnit.class)
public class MemoizationDriverExample {
  public static class Loader extends ScriptCompiler.Impl {
    public Loader() {
    }
  }

  @Import
  public final Object memo       = new MemoizationExample();
  @Import
  public final Object predicates = new Predicates();
  @Import
  public final Object arith      = new Arith();
}
