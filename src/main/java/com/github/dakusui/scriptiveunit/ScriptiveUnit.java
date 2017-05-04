package com.github.dakusui.scriptiveunit;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.Description;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.statement.Form;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.Utils.createTestClassMock;
import static com.github.dakusui.jcunit8.factorspace.Parameter.*;
import static com.github.dakusui.scriptiveunit.core.Utils.performActionWithLogging;
import static com.github.dakusui.scriptiveunit.exceptions.ResourceException.functionNotFound;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.SETUP_BEFORE_ALL;
import static com.github.dakusui.scriptiveunit.model.Stage.Type.TEARDOWN_AFTER_ALL;
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
  private final List<Runner> runners;
  private final Session      session;

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @param klass A test class.
   */
  @SuppressWarnings("unused")
  public ScriptiveUnit(Class<?> klass) throws Throwable {
    this(klass, new Config.Builder(klass, System.getProperties()).build());
  }

  /**
   * A constructor for testing.
   *
   * @param klass  A test class
   * @param config A config object.
   */
  public ScriptiveUnit(Class<?> klass, Config config) throws Throwable {
    this(klass, createTestSuiteDescriptorLoader(config));
  }

  public ScriptiveUnit(Class<?> klass, TestSuiteDescriptor.Loader loader) throws Throwable {
    super(klass);
    this.session = Session.create(loader);
    this.runners = newLinkedList(session.createTestItemRunners());
  }

  @Override
  public String getName() {
    return this.session.loadTestSuiteDescriptor().getConfig().getScriptResourceName()
        .replaceAll(".+/", "")
        .replaceAll("\\.[^.]*$", "")
        + ":" + this.session.loadTestSuiteDescriptor().getDescription();
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
        performActionWithLogging(
            createSuiteLevelAction(
                SETUP_BEFORE_ALL,
                session,
                createCommonFixture(
                    session.loadTestSuiteDescriptor().getFactorSpaceDescriptor().getParameters()
                )));
        super.evaluate();
      }
    };
  }

  @Override
  protected Statement withAfterClasses(Statement statement) {
    return new RunBefores(statement, Collections.emptyList(), null) {
      @Override
      public void evaluate() throws Throwable {
        TestSuiteDescriptor descriptor = session.loadTestSuiteDescriptor();
        super.evaluate();
        performActionWithLogging(
            createSuiteLevelAction(
                TEARDOWN_AFTER_ALL,
                session,
                createCommonFixture(descriptor.getFactorSpaceDescriptor().getParameters()))
        );
      }
    };
  }

  private static Tuple createCommonFixture(List<com.github.dakusui.jcunit8.factorspace.Parameter> parameters) {
    Tuple.Builder b = new Tuple.Builder();
    parameters.stream()
        .filter((com.github.dakusui.jcunit8.factorspace.Parameter in) -> in instanceof Simple)
        .filter((com.github.dakusui.jcunit8.factorspace.Parameter in) -> in.getKnownValues().size() == 1)
        .forEach((com.github.dakusui.jcunit8.factorspace.Parameter in) -> b.put(in.getName(), in.getKnownValues().get(0)));
    return b.build();
  }

  private static Action createSuiteLevelAction(Stage.Type stageType, Session session, Tuple commonFixture) {
    return stageType.getSuiteLevelActionFactory(session).apply(Stage.Factory.createSuiteLevelStage(stageType, session, commonFixture));
  }


  public static TestSuiteDescriptor.Loader createTestSuiteDescriptorLoader(Config config) {
    return TestSuiteDescriptor.Loader.createInstance(
        getAnnotationWithDefault(
            config.getDriverClass(),
            Load.DEFAULT_INSTANCE
        ).with(),
        config
    );
  }

  public Description describeFunction(Object driverObject, String functionName) {
    Optional<Description> value =
        Stream.concat(
            getObjectMethodsFromImportedFieldsInObject(driverObject).stream().map(ObjectMethod::describe),
            getUserDefinedFormClauses().entrySet().stream().map((Map.Entry<String, List<Object>> entry) -> Form.describe(entry.getKey(), entry.getValue()))
        ).filter(t -> functionName.equals(t.name())).findFirst();
    if (value.isPresent())
      return value.get();
    throw functionNotFound(functionName);
  }

  public List<String> getFormNames(Object driverObject) {
    return Stream.concat(
        getObjectMethodsFromImportedFieldsInObject(driverObject)
            .stream()
            .map(ObjectMethod::getName),
        getUserDefinedFormClauseNamesFromScript().stream()).collect(toList());
  }


  private List<String> getUserDefinedFormClauseNamesFromScript() {
    return getUserDefinedFormClauses().keySet().stream().collect(toList());
  }

  private Map<String, List<Object>> getUserDefinedFormClauses() {
    return this.session.loadTestSuiteDescriptor().getUserDefinedFormClauses();
  }

  public static List<ObjectMethod> getObjectMethodsFromImportedFieldsInObject(Object object) {
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
