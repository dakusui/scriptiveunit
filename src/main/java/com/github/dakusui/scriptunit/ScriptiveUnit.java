package com.github.dakusui.scriptunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.factor.Factor;
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
import com.github.dakusui.scriptunit.model.Stage;
import com.github.dakusui.scriptunit.model.TestOracle;
import com.github.dakusui.scriptunit.model.func.Func;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.Utils.createTestClassMock;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

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
    this(klass, System.getProperties());
  }

  /**
   * A constructor for testing.
   *
   * @param klass      A test class
   * @param properties A properties object. Typically a value returned by {@code System.getProperties()}.
   */
  @ReflectivelyReferenced
  protected ScriptiveUnit(Class<?> klass, Properties properties) throws Throwable {
    this(klass, createTestSuiteLoader(klass, Config.create(klass, properties).getScriptResourceName()));
  }

  protected ScriptiveUnit(Class<?> klass, TestSuiteLoader testSuiteLoader) throws Throwable {
    super(klass);
    this.testSuiteLoader = testSuiteLoader;
    this.runners = newLinkedList(createRunners(this.testSuiteLoader));
  }

  @Override
  public String getName() {
    return
        this.testSuiteLoader.getScriptResourceName()
        .replaceAll(".+/", "")
        .replaceAll("\\.[^.]*$", "")
        + ":" + this.testSuiteLoader.getDescription();
  }

  @Override
  public List<Runner> getChildren() {
    return this.runners;
  }

  @Override
  protected TestClass createTestClass(Class<?> testClass) {
    return createTestClassMock(super.createTestClass(testClass));
  }

  protected Statement withBeforeClasses(Statement statement) {
    return new RunBefores(statement, Collections.emptyList(), null) {
      @Override
      public void evaluate() throws Throwable {
        Utils.performActionWithLogging(createSetUpBeforeAllAction(
            testSuiteLoader.getSetUpBeforeAllActionFactory(),
            createCommonFixture(testSuiteLoader.getTestSuiteDescriptor().getFactorSpaceDescriptor().getFactors()))
        );
        super.evaluate();
      }

      private Tuple createCommonFixture(List<Factor> factors) {
        Tuple.Builder b = new Tuple.Builder();
        factors.stream()
            .filter((Factor in) -> in.levels.size() == 1)
            .forEach((Factor in) -> b.put(in.name, in.levels.get(0)));
        return b.build();
      }
    };
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
            return GroupedTestItemRunner.createRunnerForTestOracle(
                getTestClass().getJavaClass(),
                testSuiteLoader.getTestSuiteDescriptor().getFactorSpaceDescriptor().getFactors(),
                testSuiteLoader.getDescription(),
                id++,
                input,
                testSuiteLoader.getSetUpActionFactory(),
                testCases);
          }
        })
        .collect(toList());
  }

  Iterable<Runner> createRunnersGroupingByTestCase(final TestSuiteLoader testSuiteLoader) {
    List<TestOracle> testOracles = testSuiteLoader.loadTestOracles();
    return testSuiteLoader.loadTestCases().stream().map(
        (Function<IndexedTestCase, Runner>) (IndexedTestCase testCase) -> GroupedTestItemRunner.createRunnerForTestCase(
            getTestClass().getJavaClass(),
            testSuiteLoader.getTestSuiteDescriptor().getFactorSpaceDescriptor().getFactors(),
            testSuiteLoader.getDescription(),
            testCase.getIndex(),
            testCase,
            testSuiteLoader.getSetUpActionFactory(),
            testOracles)).collect(toList());
  }

  Iterable<Runner> createRunnersGroupingByTestFixture(TestSuiteLoader testSuiteLoader) {
    List<TestOracle> testOracles = testSuiteLoader.loadTestOracles();
    List<Factor> factors = testSuiteLoader.getTestSuiteDescriptor().getFactorSpaceDescriptor().getFactors();
    List<String> singleLevelFactors = factors.stream()
        .filter((Factor each) -> each.levels.size() == 1)
        .map((Factor each) -> each.name)
        .collect(toList());
    List<String> involved = figureOutInvolvedParameters(testSuiteLoader, singleLevelFactors);
    List<IndexedTestCase> testCases = testSuiteLoader.loadTestCases().stream()
        .sorted(byParameters(involved))
        .collect(toList());
    List<Tuple> fixtures = buildFixtures(involved, testCases);
    return fixtures.stream().map(
        (Function<Tuple, Runner>) fixture -> GroupedTestItemRunner.createRunnerForTestFixture(
            ScriptiveUnit.this.getTestClass().getJavaClass(),
            factors,
            testSuiteLoader.getDescription(),
            fixtures.indexOf(fixture),
            fixture,
            testSuiteLoader.getSetUpActionFactory(),
            testCases.stream().filter((IndexedTestCase indexedTestCase) -> ScriptiveUnit.this.project(indexedTestCase.getTuple(), involved).equals(fixture)).collect(toList()),
            testOracles
        )
    ).collect(toList());
  }

  private List<String> figureOutInvolvedParameters(TestSuiteLoader testSuiteLoader, List<String> singleLevelFactors) {
    return Stream.concat(testSuiteLoader.getTestSuiteDescriptor().getInvolvedParameterNamesInSetUpAction().stream(), singleLevelFactors.stream()).distinct().collect(toList());
  }

  private LinkedList<Tuple> buildFixtures(List<String> involved, List<IndexedTestCase> testCases) {
    return new LinkedList<>(
        testCases.stream()
            .map((Func<IndexedTestCase, Map<String, Object>>) input -> project(input.getTuple(), involved))
            .map((Func<Map<String, Object>, Tuple>) input -> new Tuple.Builder().putAll(input).build())
            .collect(toSet()));
  }

  private static Action createSetUpBeforeAllAction(Func<Stage, Action> setUpFactory, Tuple commonFixture) {
    Stage.Type stageType = Stage.Type.SETUP_BEFORE_SUITE;
    return setUpFactory.apply(stageType.create(commonFixture));
  }

  private <K, V> Map<K, V> project(Map<K, V> in, List<K> keys) {
    Map<K, V> ret = new HashMap<>();
    keys.forEach(each -> {
      if (in.containsKey(each))
        ret.put(each, in.get(each));
    });
    return ret;
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


  private static TestSuiteLoader createTestSuiteLoader(Class javaClass, String scriptResourceName) {
    Load annLoad = getAnnotationWithDefault(javaClass, Load.DEFAULT_INSTANCE);
    return TestSuiteLoader.Factory
        .create(annLoad.with())
        .create(
            scriptResourceName,
            javaClass
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

  private static <T extends Annotation> T getAnnotationWithDefault(Class javaClass, T defaultValue) {
    @SuppressWarnings("unchecked") T ret = (T) javaClass.<T>getAnnotation(defaultValue.annotationType());
    return ret != null ?
        ret :
        defaultValue;
  }
}
