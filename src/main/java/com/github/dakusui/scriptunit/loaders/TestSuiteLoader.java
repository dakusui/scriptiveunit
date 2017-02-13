package com.github.dakusui.scriptunit.loaders;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.scriptunit.ScriptRunner.Type;
import com.github.dakusui.scriptunit.core.Utils;
import com.github.dakusui.scriptunit.model.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.github.dakusui.scriptunit.core.Utils.convertIfNecessary;
import static com.github.dakusui.scriptunit.exceptions.ScriptUnitException.wrap;
import static java.util.stream.Collectors.toList;

public interface TestSuiteLoader {
  String getDescription();

  Func<Stage, Action> getSetUpActionFactory();

  Func<Stage, Action> getBeforeAllActionFactory();

  List<IndexedTestCase> loadTestCases();

  List<TestOracle> loadTestOracles();

  Type getRunnerType();

  TestSuiteDescriptor getTestSuiteDescriptor();


  abstract class Base implements TestSuiteLoader {

    private final TestSuiteDescriptor testSuiteDescriptor;

    public Base(String resourceName, Class<?> driverClass) {
      this.testSuiteDescriptor = loadTestSuite(resourceName, driverClass);
    }

    @Override
    public String getDescription() {
      return this.testSuiteDescriptor.getDescription();
    }

    @Override
    public Func<Stage, Action> getSetUpActionFactory() {
      return this.testSuiteDescriptor.getSetUpActionFactory();
    }

    @Override
    public Func<Stage, Action> getBeforeAllActionFactory() {
      return this.testSuiteDescriptor.getSetUpBeforeAllActionFactory();
    }

    @Override
    public List<IndexedTestCase> loadTestCases() {
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

    @Override
    public TestSuiteDescriptor getTestSuiteDescriptor() {
      return this.testSuiteDescriptor;
    }


    abstract protected TestSuiteDescriptor loadTestSuite(String resourceName, Class<?> driverClass);

    private List<IndexedTestCase> createTestCases(TestSuiteDescriptor testSuiteDescriptor) {
      TestSuite.Builder builder = new TestSuite.Builder(createEngine(testSuiteDescriptor.getCoveringArrayEngineConfig()));
      builder.disableNegativeTests();
      for (Factor each : testSuiteDescriptor.getFactorSpaceDescriptor().getFactors()) {
        builder.addFactor(each);
      }
      for (TestSuite.Predicate each : testSuiteDescriptor.getFactorSpaceDescriptor().getConstraints()) {
        builder.addConstraint(each);
      }
      return builder.build().getTestCases().stream()
          .map(
              new Func<TestCase, IndexedTestCase>() {
                int i = 0;

                @Override
                public IndexedTestCase apply(TestCase input) {
                  return new IndexedTestCase(i++, input);
                }
              }
          ).collect(toList());
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
    TestSuiteLoader create(String resourceName, Class<?> driverClass);

    static Factory create(Class<? extends Factory> klass) {
      try {
        return Utils.getConstructor(klass).newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw wrap(e);
      }
    }
  }
}