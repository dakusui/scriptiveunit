package com.github.dakusui.scriptunit.loaders;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.scriptunit.ScriptRunner.Type;
import com.github.dakusui.scriptunit.core.Utils;
import com.github.dakusui.scriptunit.model.CoveringArrayEngineConfig;
import com.github.dakusui.scriptunit.model.Func;
import com.github.dakusui.scriptunit.model.TestOracle;
import com.github.dakusui.scriptunit.model.TestSuiteDescriptor;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.github.dakusui.scriptunit.core.Utils.convertIfNecessary;
import static com.github.dakusui.scriptunit.exceptions.ScriptUnitException.wrap;
import static java.util.stream.Collectors.toList;

public interface TestSuiteLoader {
  String getDescription();

  List<TestCase> loadTestCases();

  List<TestOracle> loadTestOracles();

  Type getRunnerType();

  abstract class Base implements TestSuiteLoader {

    private final TestSuiteDescriptor testSuiteDescriptor;

    public Base(InputStream inputStream, Class<?> driverClass) {
      this.testSuiteDescriptor = loadTestSuite(inputStream, driverClass);
    }

    @Override
    public String getDescription() {
      return this.testSuiteDescriptor.getDescription();
    }

    @Override
    public List<TestCase> loadTestCases() {
      return this.createTestCases(this.testSuiteDescriptor);
    }

    @Override
    public List<TestOracle> loadTestOracles() {
      //noinspection unchecked
      return (List<TestOracle>) this.testSuiteDescriptor.getTestOracles();
    }

    @Override
    public Type getRunnerType() {
      return this.testSuiteDescriptor.getRunnerType();
    }


    abstract protected TestSuiteDescriptor loadTestSuite(InputStream inputStream, Class<?> driverClass);

    private List<TestCase> createTestCases(TestSuiteDescriptor testSuiteDescriptor) {
      TestSuite.Builder builder = new TestSuite.Builder(createEngine(testSuiteDescriptor.getCoveringArrayEngineConfig()));
      builder.disableNegativeTests();
      for (Factor each : testSuiteDescriptor.getFactorSpaceDescriptor().getFactors()) {
        builder.addFactor(each);
      }
      for (TestSuite.Predicate each : testSuiteDescriptor.getFactorSpaceDescriptor().getConstraints()) {
        builder.addConstraint(each);
      }
      return builder.build().getTestCases();
    }

    private CoveringArrayEngine createEngine(CoveringArrayEngineConfig coveringArrayEngineConfig) {
      try {
        Constructor<CoveringArrayEngine> constructor;
        return (constructor = Utils.getConstructor(coveringArrayEngineConfig.getEngineClass()))
            .newInstance(coveringArrayEngineConfig.getOptions().stream()
                .map(new Func<Object, Object>() {
                  int i = 0;

                  @Override
                  public Object apply(Object input) {
                    try {
                      return convertIfNecessary(input, constructor.getParameterTypes()[i]);
                    } finally {
                      i++;
                    }
                  }
                })
                .collect(toList()).toArray());
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw wrap(e);
      }
    }
  }

  interface Factory {
    TestSuiteLoader create(InputStream testSuiteName, Class<?> driverClass);

    static Factory create(Class<? extends Factory> klass) {
      try {
        return Utils.getConstructor(klass).newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw wrap(e);
      }
    }
  }
}