package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.session.Session;

import java.lang.reflect.InvocationTargetException;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static java.util.Objects.requireNonNull;

public interface TestSuiteDescriptorLoader {
  Config getConfig();

  TestSuiteDescriptor loadTestSuiteDescriptor(Session session);

  abstract class Base implements TestSuiteDescriptorLoader {

    private final Config config;

    public Base(Config config) {
      this.config = requireNonNull(config);
    }

    public Config getConfig() {
      return this.config;
    }
  }

  static TestSuiteDescriptorLoader createInstance(Class<? extends TestSuiteDescriptorLoader> klass, Config config) {
    try {
      return klass.getConstructor(Config.class).newInstance(config);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw wrap(e);
    }
  }
}
