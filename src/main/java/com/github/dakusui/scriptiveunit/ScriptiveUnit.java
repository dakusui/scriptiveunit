package com.github.dakusui.scriptiveunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.loaders.TestSuiteLoader;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.actionunit.Utils.createTestClassMock;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * A main test runner class of ScriptiveUnit.
 */
public class ScriptiveUnit extends Parameterized {
  /**
   * Test runners each of which runs a test case represented by an action.
   */
  private final List<Runner>    runners;
  private final TestSuiteLoader testSuiteLoader;

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @param klass A test class.
   */
  @ReflectivelyReferenced
  public ScriptiveUnit(Class<?> klass) throws Throwable {
    this(klass, new Config.Builder(klass, System.getProperties()).build());
  }

  /**
   * A constructor for testing.
   *
   * @param klass  A test class
   * @param config A config object.
   */
  @ReflectivelyReferenced
  protected ScriptiveUnit(Class<?> klass, Config config) throws Throwable {
    this(klass, createTestSuiteLoader(config));
  }

  public ScriptiveUnit(Class<?> klass, TestSuiteLoader testSuiteLoader) throws Throwable {
    super(klass);
    this.testSuiteLoader = testSuiteLoader;
    this.runners = newLinkedList(GroupedTestItemRunner.createRunners(this.testSuiteLoader.getTestSuiteDescriptor()));
  }

  @Override
  public String getName() {
    return this.testSuiteLoader.getScriptResourceName()
        .replaceAll(".+/", "")
        .replaceAll("\\.[^.]*$", "")
        + ":" + this.testSuiteLoader.getTestSuiteDescriptor().getDescription();
  }


  @Override
  public List<Runner> getChildren() {
    return this.runners;
  }

  @Override
  protected TestClass createTestClass(Class<?> testClass) {
    return createTestClassMock(super.createTestClass(testClass));
  }

  @Override
  protected Statement withBeforeClasses(Statement statement) {
    return new RunBefores(statement, Collections.emptyList(), null) {
      @Override
      public void evaluate() throws Throwable {
        Utils.performActionWithLogging(createSetUpBeforeAllAction(
            testSuiteLoader.getTestSuiteDescriptor(),
            createCommonFixture(testSuiteLoader.getTestSuiteDescriptor().getFactorSpaceDescriptor().getFactors()))
        );
        super.evaluate();
      }
    };
  }

  @Override
  protected Statement withAfterClasses(Statement statement) {
    return new RunBefores(statement, Collections.emptyList(), null) {
      @Override
      public void evaluate() throws Throwable {
        super.evaluate();
        Utils.performActionWithLogging(createTearDownAfterAllAction(
            testSuiteLoader.getTestSuiteDescriptor(),
            createCommonFixture(testSuiteLoader.getTestSuiteDescriptor().getFactorSpaceDescriptor().getFactors()))
        );
      }
    };
  }

  private static Tuple createCommonFixture(List<Factor> factors) {
    Tuple.Builder b = new Tuple.Builder();
    factors.stream()
        .filter((Factor in) -> in.levels.size() == 1)
        .forEach((Factor in) -> b.put(in.name, in.levels.get(0)));
    return b.build();
  }

  private static Action createSetUpBeforeAllAction(TestSuiteDescriptor testSuiteDescriptor, Tuple commonFixture) {
    return testSuiteDescriptor.getSetUpBeforeAllActionFactory().apply(Stage.Type.SETUP_BEFORE_ALL.create(testSuiteDescriptor, commonFixture, null));
  }

  private static Action createTearDownAfterAllAction(TestSuiteDescriptor testSuiteDescriptor, Tuple commonFixture) {
    return testSuiteDescriptor.getTearDownAfterAllActionFactory().apply(Stage.Type.TEARDOWN_AFETR_ALL.create(testSuiteDescriptor, commonFixture, null));
  }


  private static TestSuiteLoader createTestSuiteLoader(Config config) {
    return TestSuiteLoader.Factory
        .create(getAnnotationWithDefault(config.getDriverClass(), Load.DEFAULT_INSTANCE).with())
        .create(config);
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

  private static <T extends Annotation> T getAnnotationWithDefault(Class javaClass, T defaultValue) {
    @SuppressWarnings("unchecked") T ret = (T) javaClass.<T>getAnnotation(defaultValue.annotationType());
    return ret != null ?
        ret :
        defaultValue;
  }
}
