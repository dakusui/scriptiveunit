package com.github.dakusui.scriptiveunit.loaders;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.scriptiveunit.GroupedTestItemRunner.Type;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.*;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.github.dakusui.scriptiveunit.core.Utils.convertIfNecessary;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static java.util.stream.Collectors.toList;

public interface TestSuiteLoader {
  String getScriptResourceName();

  String getDescription();

  Func<Stage, Action> getSetUpActionFactory();

  Func<Stage, Action> getSetUpBeforeAllActionFactory();

  List<IndexedTestCase> loadTestCases();

  List<TestOracle> loadTestOracles();

  Type getRunnerType();

  TestSuiteDescriptor getTestSuiteDescriptor();


  abstract class Base implements TestSuiteLoader {

    private final TestSuiteDescriptor testSuiteDescriptor;
    private final String              scriptResourceName;

    public Base(String scriptResourceName, Class<?> driverClass) {
      this.scriptResourceName = scriptResourceName;
      this.testSuiteDescriptor = loadTestSuite(driverClass, scriptResourceName);
    }

    @Override
    public String getScriptResourceName() {
      return this.scriptResourceName;
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
    public Func<Stage, Action> getSetUpBeforeAllActionFactory() {
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

    private List<IndexedTestCase> createTestCases(TestSuiteDescriptor testSuiteDescriptor) {
      FactorSpaceDescriptor factorSpaceDescriptor = testSuiteDescriptor.getFactorSpaceDescriptor();
      CoveringArrayEngineConfig coveringArrayEngineConfig = testSuiteDescriptor.getCoveringArrayEngineConfig();

      TestSuite.Builder builder = new TestSuite.Builder(createEngine(coveringArrayEngineConfig));
      builder.disableNegativeTests();
      if (!factorSpaceDescriptor.getFactors().isEmpty()) {
        factorSpaceDescriptor.getFactors().forEach(builder::addFactor);
      } else {
        builder.addFactor("*dummyFactor*", "*dummyLevel*");
      }

      for (TestSuite.Predicate each : factorSpaceDescriptor.getConstraints()) {
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
    abstract protected TestSuiteDescriptor loadTestSuite(Class<?> driverClass, String resourceName);
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