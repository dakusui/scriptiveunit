package com.github.dakusui.scriptiveunit;

import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ScriptiveSuiteSet extends ParentRunner<Runner> {
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Inherited
  public @interface SuiteScripts {
    Class<?> driverClass();

    /**
     * @return the scripts to be run
     */
    String[] includes() default {};

    String[] excludes() default {};
  }

  private List<Runner> runners;

  /**
   * Called reflectively on classes annotated with <code>@RunWith(Suite.class)</code>
   *
   * @param klass   the root class
   * @param builder builds runners for classes in the suite
   */
  @ReflectivelyReferenced
  public ScriptiveSuiteSet(Class<?> klass, RunnerBuilder builder) throws InitializationError {
    this(
        klass,
        targetScripts(klass.getAnnotation(SuiteScripts.class))
            .map(
                scriptResourceName ->
                    createRunner(scriptResourceName, figureOutDriverClass(klass)))
            .collect(toList()));
  }

  private static Stream<String> targetScripts(SuiteScripts suiteScripts) {
    return allScripts()
        .filter(matchesAnyOf(toPatterns(suiteScripts.includes())))
        .filter(not(matchesAnyOf(toPatterns(suiteScripts.excludes()))));
  }

  private static List<Pattern> toPatterns(String[] patterns) {
    return Arrays.stream(patterns).map(Pattern::compile).collect(toList());
  }

  private static Predicate<String> matchesAnyOf(List<Pattern> patterns) {
    return s -> {
      for (Pattern each : patterns) {
        if (each.matcher(s).matches())
          return true;
      }
      return false;
    };
  }

  private static Predicate<String> not(Predicate<String> input) {
    return s -> !input.test(s);
  }

  private static Stream<String> allScripts() {
    return new Reflections(Load.DEFAULT_SCRIPT_PACKAGE_PREFIX, new ResourcesScanner()).getResources(Pattern.compile(Load.DEFAULT_SCRIPT_NAME_PATTERN)).stream();
  }


  private static Class<?> figureOutDriverClass(Class<?> klass) {
    return klass.getAnnotation(SuiteScripts.class).driverClass();
  }

  private static Runner createRunner(String scriptResourceName, Class<?> klass) {
    try {
      return new ScriptiveUnit(klass, new Config.Builder(klass, System.getProperties()).withScriptResourceName(scriptResourceName).build());
    } catch (Error | RuntimeException e) {
      throw e;
    } catch (Throwable throwable) {
      throw ScriptiveUnitException.wrap(throwable);
    }
  }

  /**
   * Called by this class and subclasses once the runners making up the suite have been determined
   *
   * @param klass   root of the suite
   * @param runners for each class in the suite, a {@link Runner}
   */
  protected ScriptiveSuiteSet(Class<?> klass, List<Runner> runners) throws InitializationError {
    super(klass);
    this.runners = Collections.unmodifiableList(runners);
  }

  @Override
  protected List<Runner> getChildren() {
    return this.runners;
  }

  @Override
  protected Description describeChild(Runner child) {
    return child.getDescription();
  }

  @Override
  protected void runChild(Runner runner, RunNotifier notifier) {
    runner.run(notifier);
  }
}