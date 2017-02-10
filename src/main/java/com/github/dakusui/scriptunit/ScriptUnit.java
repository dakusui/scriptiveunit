package com.github.dakusui.scriptunit;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.scriptunit.annotations.Import;
import com.github.dakusui.scriptunit.annotations.Load;
import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.annotations.Scriptable;
import com.github.dakusui.scriptunit.core.Config;
import com.github.dakusui.scriptunit.core.ObjectMethod;
import com.github.dakusui.scriptunit.core.Utils;
import com.github.dakusui.scriptunit.loaders.IndexedTestCase;
import com.github.dakusui.scriptunit.loaders.TestSuiteLoader;
import com.github.dakusui.scriptunit.model.Func;
import com.github.dakusui.scriptunit.model.TestOracle;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

import static com.github.dakusui.actionunit.Utils.createTestClassMock;
import static com.github.dakusui.scriptunit.exceptions.ScriptUnitException.wrap;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

/**
 * A main test runner class of ScriptUnit.
 */
public class ScriptUnit extends Parameterized {
  /**
   * Test runners each of which runs a test case represented by an action.
   */
  private final List<Runner> runners;
  private final Properties   properties;

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @param klass A test class.
   */
  @ReflectivelyReferenced
  public ScriptUnit(Class<?> klass) throws Throwable {
    this(klass, System.getProperties());
  }

  /**
   * A constructor for testing.
   *
   * @param klass      A test class
   * @param properties A properties object. Typically a value returned by {@code System.getProperties()}.
   */
  @ReflectivelyReferenced
  protected ScriptUnit(Class<?> klass, Properties properties) throws Throwable {
    super(klass);
    this.properties = requireNonNull(properties);
    try {
      runners = newLinkedList(createRunners(createTestSuiteLoader(this.getTestClass())));
    } catch (RuntimeException e) {
      if (e.getCause() instanceof InitializationError) {
        throw e.getCause();
      }
      throw e;
    }
  }

  @Override
  public List<Runner> getChildren() {
    return this.runners;
  }

  @Override
  protected TestClass createTestClass(Class<?> testClass) {
    return createTestClassMock(super.createTestClass(testClass));
  }

  private Iterable<Runner> createRunners(TestSuiteLoader testSuiteLoader) {
    return testSuiteLoader.getRunnerType().createRunners(this, testSuiteLoader);
  }

  Iterable<Runner> createRunnersGroupingByTestOracle(final TestSuiteLoader testSuiteLoader) {
    List<IndexedTestCase> testCases = testSuiteLoader.loadTestCases();
    return testSuiteLoader.loadTestOracles()
        .stream()
        .map(new Func<TestOracle, Runner>() {
          int id = 0;

          @Override
          public Runner apply(TestOracle input) {
            try {
              return new ScriptRunner.GroupingByTestOracle(
                  getTestClass().getJavaClass(),
                  testSuiteLoader.getDescription(),
                  id++,
                  input,
                  testSuiteLoader.getSetUpActionFactory(),
                  testCases);
            } catch (InitializationError initializationError) {
              throw wrap(initializationError);
            }
          }
        })
        .collect(toList());
  }

  Iterable<Runner> createRunnersGroupingByTestCase(final TestSuiteLoader testSuiteLoader) {
    List<TestOracle> testOracles = testSuiteLoader.loadTestOracles();
    return testSuiteLoader.loadTestCases().stream().map(
        (Function<IndexedTestCase, Runner>) (IndexedTestCase testCase) -> {
          try {
            return new ScriptRunner.GroupingByTestCase(
                getTestClass().getJavaClass(),
                testSuiteLoader.getDescription(),
                testCase.getIndex(),
                testCase,
                testSuiteLoader.getSetUpActionFactory(),
                testOracles);
          } catch (InitializationError initializationError) {
            throw wrap(initializationError);
          }
        }).collect(toList());
  }

  Iterable<Runner> createRunnersGroupingByTestFixture(TestSuiteLoader testSuiteLoader) {
    List<TestOracle> testOracles = testSuiteLoader.loadTestOracles();
    List<String> involved = testSuiteLoader.getInvolvedParameterNamesInSetUpAction();
    List<IndexedTestCase> testCases = testSuiteLoader.loadTestCases().stream()
        .sorted(byParameters(involved))
        .collect(toList());
    Set<Tuple> fixtures = testCases.stream()
        .map((Func<IndexedTestCase, Tuple>) input -> new Tuple.Builder().putAll
            (input.getTuple().entrySet().stream()
                .filter(input1 -> involved.contains(input1.getKey()))
                .collect(toList())
                .stream()
                .collect(
                    toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                    ))).build())
        .collect(toSet());
    return null;
  }

  private Comparator<? super TestCase> byParameters(List<String> parameters) {
    return (Comparator<TestCase>) (o1, o2) -> {
      for (String each : parameters) {
        int ret = Objects.toString(o1.getTuple().get(each)).compareTo(Objects.toString(o2.getTuple().get(each)));
        if (ret != 0)
          return ret;
      }
      return 0;
    };
  }


  private TestSuiteLoader createTestSuiteLoader(TestClass testClass) {
    Load annLoad = getAnnotationWithDefault(testClass, Load.DEFAULT_INSTANCE);
    return TestSuiteLoader.Factory
        .create(annLoad.with())
        .create(
            Config.create(testClass.getJavaClass(), this.properties).getScriptResourceName(),
            testClass.getJavaClass()
        );
  }

  public static List<ObjectMethod> getAnnotatedMethodsFromImportedFieldsInObject(Object object) {
    return Utils.getAnnotatedFields(object, Import.class)
        .stream()
        .map(each -> Utils.getAnnotatedMethods(
            each.get(),
            Scriptable.class,
            createAliasMap(each.getField().getAnnotation(Import.class).value()))
        )
        .flatMap(List::stream)
        .filter(objectMethod -> objectMethod.getName() != null)
        .collect(toList());
  }

  private static Map<String, String> createAliasMap(Import.Alias[] aliases) {
    return Arrays.stream(
        aliases
    ).collect(toMap(alias -> requireNonNull(alias).value(), alias -> !"".equals(requireNonNull(alias).as()) ? alias.as() : ""
    ));
  }

  private static <T extends Annotation> T getAnnotationWithDefault(TestClass testClass, T defaultValue) {
    @SuppressWarnings("unchecked") T ret = testClass.getAnnotation((Class<? extends T>) defaultValue.annotationType());
    return ret != null ?
        ret :
        defaultValue;
  }
}
