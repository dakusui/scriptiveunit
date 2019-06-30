package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.drivers.Core;
import com.github.dakusui.scriptiveunit.drivers.Predicates;
import com.github.dakusui.scriptiveunit.drivers.Strings;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.atom;

@RunWith(ScriptiveUnit.class)
@Load(with = NoScript.Loader.class)
public class NoScript {
  interface SyntaxSugar {
    default ApplicationSpec.Dictionary dict(ApplicationSpec.Dictionary.Entry... entries) {
      return ApplicationSpec.dict(entries);
    }

    default ApplicationSpec.Array array(Object... values) {
      return ApplicationSpec.array((ApplicationSpec.Node[])
          Arrays.stream(values)
              .map(each -> each instanceof ApplicationSpec.Node ?
                  ((ApplicationSpec.Node) each) :
                  ApplicationSpec.atom(each))
              .toArray(ApplicationSpec.Node[]::new));
    }

    default ApplicationSpec.Dictionary.Entry entry(String key, Object value) {
      return ApplicationSpec.entry(key,
          value instanceof ApplicationSpec.Node ?
              (ApplicationSpec.Node) value :
              atom(value));
    }

    default ApplicationSpec.Dictionary.Entry $(String key, Object value) {
      return this.entry(key, value);
    }
  }

  public static class Loader extends TestSuiteDescriptorLoader.Base implements SyntaxSugar {
    public Loader(Config config) {
      super(config);
    }

    @Override
    protected ApplicationSpec applicationLanguage() {
      return new ApplicationSpec.Standard();
    }

    @Override
    protected HostSpec hostLanguage() {
      return new HostSpec.Json();
    }

    @Override
    protected ApplicationSpec.Dictionary readScript(Config config, ApplicationSpec.Dictionary defaultValues) {
      return applicationSpec.deepMerge(
          dict(
              $("testOracles", array(
                  dict(
                      $("when", array("format", "hello")),
                      $("then", array("matches", array("output"), ".*ell.*"))),
                  dict(
                      $("when", array("format", "hello")),
                      $("then", array("matches", array("output"), ".*ELLO"))),
                  dict(
                      $("given", array("not", array("always"))),
                      $("when", array("format", "hello")),
                      $("then", array("matches", array("output"), ".*Ell.*")))))),
          defaultValues);
    }
  }

  @Import
  public Object core    = new Core();
  @Import
  public Object predicates = new Predicates();
  @Import
  public Object strings = new Strings();
}
