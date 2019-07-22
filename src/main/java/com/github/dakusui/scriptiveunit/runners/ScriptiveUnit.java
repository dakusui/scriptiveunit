package com.github.dakusui.scriptiveunit.runners;

import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.core.Script;
import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;
import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.session.Session;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.Collections;
import java.util.List;

import static com.github.dakusui.jcunit8.core.Utils.createTestClassMock;
import static com.github.dakusui.scriptiveunit.annotations.Utils.createScriptCompilerFrom;
import static com.github.dakusui.scriptiveunit.annotations.Utils.createScriptLoaderFrom;
import static com.github.dakusui.scriptiveunit.utils.ActionUtils.performActionWithLogging;
import static com.github.dakusui.scriptiveunit.utils.ReflectionUtils.getAnnotation;
import static com.google.common.collect.Lists.newLinkedList;

/**
 * A createAction test runner class of ScriptiveUnit.
 */
public class ScriptiveUnit extends Parameterized {
  /**
   * Test runners each of which runs a test case represented by an action.
   */
  private final List<Runner> runners;
  private final Session session;

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @param klass A test class.
   */
  @SuppressWarnings("unused")
  public ScriptiveUnit(Class<?> klass) throws Throwable {
    this(klass,
        createScriptCompilerFrom(getAnnotation(klass, RunScript.class).orElseThrow(RuntimeException::new).compiler()),
        createScriptLoaderFrom(getAnnotation(klass, RunScript.class).orElseThrow(RuntimeException::new).loader()).load(klass)
    );
  }

  public ScriptiveUnit(Class<?> klass, ScriptCompiler scriptCompiler, Script script) throws Throwable {
    super(klass);
    this.session = Session.create(script, scriptCompiler);
    this.runners = newLinkedList(createRunners());
  }

  public TestSuiteDescriptor getTestSuiteDescriptor() {
    return this.session.getTestSuiteDescriptor();
  }

  @Override
  public String getName() {
    return this.session.getScript()
        .name()
        .replaceAll(".+/", "")
        .replaceAll("\\.[^.]*$", "")
        + ":" + getTestSuiteDescriptor().getDescription();
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
        performActionWithLogging(session.createSetUpBeforeAllAction());
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
        performActionWithLogging(session.createTearDownAfterAllAction());
      }
    };
  }

  private Iterable<Runner> createRunners() {
    return getTestSuiteDescriptor().getRunnerMode().createRunners(this.session);
  }
}
