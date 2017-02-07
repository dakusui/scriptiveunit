package com.github.dakusui.scriptunit;

import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.scriptunit.annotations.Import;
import com.github.dakusui.scriptunit.annotations.Load;
import com.github.dakusui.scriptunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptunit.annotations.Scriptable;
import com.github.dakusui.scriptunit.core.ObjectMethod;
import com.github.dakusui.scriptunit.core.Utils;
import com.github.dakusui.scriptunit.exceptions.ConfigurationException;
import com.github.dakusui.scriptunit.exceptions.ResourceException;
import com.github.dakusui.scriptunit.loaders.TestSuiteLoader;
import com.github.dakusui.scriptunit.model.Func;
import com.github.dakusui.scriptunit.model.TestOracle;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static com.github.dakusui.actionunit.Utils.createTestClassMock;
import static com.github.dakusui.scriptunit.annotations.Load.SCRIPT_NOT_SPECIFIED;
import static com.github.dakusui.scriptunit.core.Utils.check;
import static com.github.dakusui.scriptunit.core.Utils.createTestCase;
import static com.github.dakusui.scriptunit.exceptions.ScriptUnitException.wrap;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
    ScriptRunner.Type runnerType = testSuiteLoader.getRunnerType();
    if (ScriptRunner.Type.GROUP_BY_TEST_CASE.equals(runnerType)) {
      return createRunnersGroupingByTestCase(testSuiteLoader);
    } else if (ScriptRunner.Type.GROUP_BY_TEST_ORACLE.equals(runnerType)) {
      return createRunnersGroupingByTestOracle(testSuiteLoader);
    }
    throw ConfigurationException.unsupportedRunMode(runnerType);
  }

  private Iterable<Runner> createRunnersGroupingByTestOracle(final TestSuiteLoader testSuiteLoader) {
    List<TestCase> testCases = testSuiteLoader.loadTestCases();
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
                  testCases.stream()
                      .map(Utils::createTestCase)
                      .collect(toList())
              );
            } catch (InitializationError initializationError) {
              throw wrap(initializationError);
            }
          }
        })
        .collect(toList());
  }

  private Iterable<Runner> createRunnersGroupingByTestCase(final TestSuiteLoader testSuiteLoader) {
    List<TestOracle> testOracles = testSuiteLoader.loadTestOracles();
    return testSuiteLoader.loadTestCases().stream().map(new Function<TestCase, Runner>() {
      int id = 0;

      @Override
      public Runner apply(TestCase testCase) {
        try {
          return new ScriptRunner.GroupingByTestCase(
              getTestClass().getJavaClass(),
              testSuiteLoader.getDescription(),
              id++,
              createTestCase(testCase),
              testOracles);
        } catch (InitializationError initializationError) {
          throw wrap(initializationError);
        }
      }
    }).collect(toList());
  }

  private TestSuiteLoader createTestSuiteLoader(TestClass testClass) {
    Load annLoad = getAnnotationWithDefault(testClass, Load.DEFAULT_INSTANCE);
    return TestSuiteLoader.Factory
        .create(annLoad.with())
        .create(
            openResourceAsStream(getScriptName(this.properties, annLoad)),
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

  private static InputStream openResourceAsStream(String resourceName) {
    return requireNonNull(getSystemResourceAsStream(resourceName), format("Failed to open '%s'. Make sure it is available on your classpath.", resourceName));
  }

  private static <T extends Annotation> T getAnnotationWithDefault(TestClass testClass, T defaultValue) {
    @SuppressWarnings("unchecked") T ret = testClass.getAnnotation((Class<? extends T>) defaultValue.annotationType());
    return ret != null ?
        ret :
        defaultValue;
  }

  private static String getScriptName(Properties properties, Load annLoad) {
    String scriptName = properties.getProperty(annLoad.scriptSystemProperty(), annLoad.defaultScriptName());
    check(!SCRIPT_NOT_SPECIFIED.equals(scriptName), ResourceException::scriptNotSpecified);
    return scriptName;
  }
}
