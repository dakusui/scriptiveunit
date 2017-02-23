package com.github.dakusui.scriptiveunit.doc;

import com.github.dakusui.actionunit.visitors.ActionPrinter.Writer;
import com.github.dakusui.actionunit.visitors.ActionPrinter.Writer.Std;
import com.github.dakusui.scriptiveunit.GroupedTestItemRunner;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBasedTestSuiteLoader;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.github.dakusui.scriptiveunit.ScriptiveUnit.getAnnotatedMethodsFromImportedFieldsInObject;
import static com.github.dakusui.scriptiveunit.core.Utils.*;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface Help {
  static void help(Class<?> testClass, Writer stdout, Writer stderr, String... args) {
    boolean succeeded = false;
    try {
      if (args.length == 0)
        help(testClass, stdout);
      else if (args.length == 1)
        help(testClass, stdout, EntityType.valueOf(args[0]));
      else if (args.length == 2)
        help(testClass, stdout, EntityType.valueOf(args[0]), args[1]);
      else
        throw new IllegalArgumentException("Too many arguments");
      succeeded = true;
    } finally {
      if (!succeeded)
        help(testClass, stderr);
    }
  }

  static void help(Class<?> testClass, String... args) {
    help(testClass, Std.OUT, Std.ERR, args);
  }

  static void help(Class<?> testClass, Writer writer) {
    String className = testClass.getCanonicalName();
    String scriptSystemPropertyKey = new Config.Builder(testClass, System.getProperties()).build().getScriptResourceNameKey();
    writer.writeLine(format(
        "This is a test class " + className + ".%n"
            + "You can run this as a JUnit test class from your IDE, build tool, etc.%n%n"
            + " Use -D" + scriptSystemPropertyKey + "={path to your script on your class path}"
            + " to specify script to be run.%n%n"
            + "By running this as an application, you can see this message and other helps."
            + " To list all the available scripts on your classpath, run%n%n"
            + "    " + className + " help " + EntityType.SCRIPT + "%n%n"
            + " To list all the available functions that can be used in your scripts, run%n%n"
            + "    " + className + " help " + EntityType.FUNCTION + "%n%n"
            + " To list all the available runners by which you can run your scripts, run%n%n"
            + "    " + className + " help " + EntityType.RUNNER + "%n%n"
            + " To describe, a script, a function, or a runner, run %n%n"
            + "    " + className + " help " + EntityType.SCRIPT + " {script name}%n"
            + "    " + className + " help " + EntityType.FUNCTION + " {function name}%n"
            + "    " + className + " help " + EntityType.RUNNER + " {runner name}%n"
            + "%n"));
  }

  static void help(Class<?> testClass, Writer writer, EntityType type) {
    create(testClass, type)
        .list()
        .forEach(writer::writeLine);
  }

  static void help(Class<?> testClass, Writer writer, EntityType type, String name) {
    Description description = create(testClass, type).describe(name);
    writeDescription(writer, 0, description);
  }

  static void writeDescription(Writer writer, int level, Description description) {
    StringBuilder b = new StringBuilder();
    b.append(format("%s: ", description.name()));
    description.content().forEach(b::append);
    writer.writeLine(indent(level) + b.toString());
    description.children().forEach(input -> writeDescription(writer, level + 1, input));
  }

  List<String> list();

  Description describe(String name);

  static Help create(Class<?> driverClass, EntityType entityType) {
    return requireNonNull(entityType).create(requireNonNull(driverClass));
  }

  enum EntityType {
    FUNCTION {
      @Override
      public Help create(Class<?> driverClass) {
        try {
          Object object = driverClass.newInstance();
          return new Help() {
            List<ObjectMethod> functions = sort(
                getAnnotatedMethodsFromImportedFieldsInObject(object),
                comparing(ObjectMethod::getName));

            @Override
            public List<String> list() {
              return this.functions.stream()
                  .map(ObjectMethod::getName)
                  .collect(toList());
            }

            @Override
            public Description describe(String name) {
              Optional<ObjectMethod> target = functions.stream().filter(
                  (ObjectMethod input) -> name.equals(input.getName())
              ).findFirst();
              final ObjectMethod objectMethod;
              if (target.isPresent())
                objectMethod = target.get();
              else
                throw new ScriptiveUnitException("Framework error");

              return new Description() {
                @Override
                public String name() {
                  return objectMethod.getName();
                }

                @Override
                public List<String> content() {
                  return asList(objectMethod.doc().value());
                }

                @Override
                public List<Description> children() {
                  return new AbstractList<Description>() {
                    @Override
                    public int size() {
                      return objectMethod.getParameterCount();
                    }

                    @Override
                    public Description get(int index) {
                      return new Description() {
                        @Override
                        public String name() {
                          return format("[%d]", index);
                        }

                        @Override
                        public List<String> content() {
                          return asList(objectMethod.getParameterDoc(index).value());
                        }
                      };
                    }
                  };
                }
              };
            }
          };
        } catch (IllegalAccessException | InstantiationException e) {
          throw wrap(e);
        }
      }
    },
    SCRIPT {
      @Override
      public Help create(Class<?> driverClass) {
        Load loadAnn = getAnnotation(driverClass, Load.class, Load.DEFAULT_INSTANCE);

        Reflections reflections = new Reflections(loadAnn.scriptPackagePrefix(), new ResourcesScanner());
        return new Help() {
          @Override
          public List<String> list() {
            return new LinkedList<>(reflections.getResources(Pattern.compile(loadAnn.scriptNamePattern())));
          }

          @Override
          public Description describe(String name) {
            return new Description() {
              @Override
              public String name() {
                return name;
              }

              @Override
              public List<String> content() {
                try {
                  Config config = new Config.Builder(driverClass, System.getProperties())
                      .withScriptResourceName(name)
                      .build();
                  return singletonList(new JsonBasedTestSuiteLoader(config) {
                  }.getTestSuiteDescriptor().getDescription());
                } catch (Exception e) {
                  throw wrap(e);
                }
              }
            };
          }
        };
      }
    },
    RUNNER {
      @Override
      public Help create(Class<?> driverClass) {
        return new Help() {
          @Override
          public List<String> list() {
            return stream(GroupedTestItemRunner.Type.values()).map(GroupedTestItemRunner.Type::name).map(Utils::toCamelCase).collect(toList());
          }

          @Override
          public Description describe(String name) {
            return new Description() {
              @Override
              public String name() {
                return name;
              }

              @Override
              public List<String> content() {
                try {
                  return asList(Utils.getAnnotation(GroupedTestItemRunner.Type.class.getField(toALL_CAPS(name)), Doc.class, Doc.NOT_AVAILABLE).value());
                } catch (NoSuchFieldException e) {
                  throw ScriptiveUnitException.wrap(e);
                }
              }
            };
          }
        };
      }
    };

    public abstract Help create(Class<?> driverClass);
  }
}
