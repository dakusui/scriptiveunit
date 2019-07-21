package com.github.dakusui.scriptiveunit.runners;

import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;
import com.github.dakusui.scriptiveunit.utils.ReflectionUtils;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class ScriptiveSuiteSet extends ParentRunner<Runner> {

  public static final  String            SCRIPTIVEUNIT_PARTITION    = "scriptiveunit.partition";
  private static final Predicate<String> TARGET_PARTITION_PREDICATE = createIsInTargetPartitionPredicate();

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

    String prefix() default "";

    class Streamer {
      private final SuiteScripts annotationInstance;

      public Streamer(SuiteScripts ann) {
        this.annotationInstance = ann;
      }

      public Stream<String> stream() {
        return targetScripts(this.annotationInstance);
      }

      private static Stream<String> targetScripts(SuiteScripts suiteScripts) {
        return ReflectionUtils.allScriptsUnder(suiteScripts.prefix())
            .filter(matchesAnyOf(toPatterns(suiteScripts.includes())))
            .filter(not(matchesAnyOf(toPatterns(suiteScripts.excludes()))))
            .filter(isInTargetPartition())
            .sorted();
      }
    }
  }

  private List<Runner> runners;

  /**
   * Called reflectively on classes annotated with <code>@RunWith(Suite.class)</code>
   *
   * @param klass   the root class
   * @param builder builds runners for classes in the suite
   */
  @SuppressWarnings({ "unused" })
  public ScriptiveSuiteSet(Class<?> klass, RunnerBuilder builder) throws InitializationError {
    this(
        klass,
        new SuiteScripts.Streamer(klass.getAnnotation(SuiteScripts.class)).stream()
            .map(
                scriptResourceName ->
                    createRunner(scriptResourceName, figureOutDriverClass(klass)))
            .collect(toList()));
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

  private static Predicate<? super String> isInTargetPartition() {
    return TARGET_PARTITION_PREDICATE;
  }

  private static Predicate<String> createIsInTargetPartitionPredicate() {
    Properties properties = System.getProperties();
    if (!properties.containsKey(SCRIPTIVEUNIT_PARTITION))
      return s -> true;
    String partitionDef = System.getProperty(SCRIPTIVEUNIT_PARTITION);
    int partitionId = Integer.parseInt(partitionDef.substring(0, partitionDef.indexOf(':')));
    int numPartitions = Integer.parseInt(partitionDef.substring(partitionDef.indexOf(':') + 1));
    if (partitionId >= numPartitions)
      throw new NumberFormatException(format(
          "'%s' was given as partition id, but it was not less than the number of partitions: '%s'",
          partitionId,
          numPartitions));
    return s -> Math.abs(s.hashCode()) % numPartitions == partitionId;
  }

  private static Class<?> figureOutDriverClass(Class<?> klass) {
    return klass.getAnnotation(SuiteScripts.class).driverClass();
  }

  private static Runner createRunner(String scriptResourceName, Class<?> klass) {
    try {
      JsonScript.Compat script = new JsonScript.Compat(klass, System.getProperties(), scriptResourceName);
      return new ScriptiveUnit(
          klass,
          new ScriptCompiler.Default(),
          script);
    } catch (Error | RuntimeException e) {
      throw e;
    } catch (Throwable throwable) {
      throw ScriptiveUnitException.wrapIfNecessary(throwable);
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
