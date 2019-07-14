package com.github.dakusui.scriptiveunit.featuretests;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.drivers.Core;
import com.github.dakusui.scriptiveunit.drivers.Predicates;
import com.github.dakusui.scriptiveunit.drivers.Strings;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteDescriptorLoader;
import com.github.dakusui.scriptiveunit.model.form.Value;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec;
import com.github.dakusui.scriptiveunit.loaders.preprocessing.HostSpec;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec.atom;

@RunWith(ScriptiveUnit.class)
public abstract class SimpleTestBase {
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

  public abstract static class Loader extends TestSuiteDescriptorLoader.Base implements SyntaxSugar {
    Loader(Config config) {
      super(config);
    }

    @Override
    protected ApplicationSpec createApplicationSpec() {
      return new ApplicationSpec.Standard();
    }

    @Override
    protected HostSpec createHostSpec() {
      return new HostSpec.Json();
    }
  }

  @Import
  public Object core = new Core();
  @Import
  public Object predicates = new Predicates();
  @Import
  public Object strings = new Strings();

  @Import
  public Object broken = new Broken();
  public static class Broken {
    @Scriptable
    public Value<String> brokenForm() {
        throw new RuntimeException("brokenForm");
    }
  }
}
