package com.github.dakusui.scriptiveunit.doc;

import com.github.dakusui.scriptiveunit.GroupedTestItemRunner;
import com.github.dakusui.scriptiveunit.ScriptiveSuiteSet;
import com.github.dakusui.scriptiveunit.ScriptiveSuiteSet.SuiteScripts;
import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.Session;
import com.github.dakusui.scriptiveunit.annotations.Doc;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.core.ObjectMethod;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBasedLoader;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;

import java.util.AbstractList;
import java.util.List;
import java.util.Optional;

import static com.github.dakusui.scriptiveunit.ScriptiveUnit.getObjectMethodsFromImportedFieldsInObject;
import static com.github.dakusui.scriptiveunit.core.Utils.*;
import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static java.lang.Class.forName;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface Documentation {

  List<String> list();

  Description describe(String name);

  static Documentation create(Class<?> driverClass, EntryType entryType) {
    return requireNonNull(entryType).create(requireNonNull(driverClass));
  }

  enum EntryType {
    FUNCTION {
      @Override
      public Documentation create(Class<?> driverClass) {
        try {
          Object object = driverClass.newInstance();
          return new Documentation() {
            List<ObjectMethod> functions = sort(
                getObjectMethodsFromImportedFieldsInObject(object),
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
    DRIVER {
      @Override
      public Documentation create(Class<?> driverClass) {
        return new Documentation() {
          @Override
          public List<String> list() {
            return getRunnerClasses(ScriptiveUnit.class);
          }

          @Override
          public Description describe(String name) {
            return createDescriptionFromSpecifiedClass(name);
          }
        };
      }
    },
    SUITESET {
      @Override
      public Documentation create(Class<?> none) {
        return new Documentation() {
          @Override
          public List<String> list() {
            return getRunnerClasses(ScriptiveSuiteSet.class);
          }

          @Override
          public Description describe(String name) {
            return createDescriptionFromSpecifiedClass(name);
          }
        };
      }
    },
    SCRIPT {
      @Override
      public Documentation create(Class<?> suiteSetClass) {
        if (suiteSetClass.isAnnotationPresent(RunWith.class)) {
          Class<? extends Runner> runnerClass = suiteSetClass.getAnnotation(RunWith.class).value();
          if (runnerClass.equals(ScriptiveSuiteSet.class)) {
            if (!suiteSetClass.isAnnotationPresent(SuiteScripts.class))
              throw new ScriptiveUnitException(format(
                  "'%s' is not annotated with '%s'",
                  suiteSetClass.getCanonicalName(),
                  SuiteScripts.class.getCanonicalName()
              ));
            SuiteScripts loadAnn = getAnnotation(suiteSetClass, SuiteScripts.class, null);
            return new Documentation() {
              @Override
              public List<String> list() {
                return new SuiteScripts.Streamer(loadAnn).stream().collect(toList());
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
                      Config config = new Config.Builder(suiteSetClass, System.getProperties())
                          .withScriptResourceName(name)
                          .build();
                      return singletonList(Session.create(new JsonBasedLoader(config) {
                      }).getDescriptor().getDescription());
                    } catch (Exception e) {
                      throw wrap(e);
                    }
                  }
                };
              }
            };
          }
          throw new ScriptiveUnitException(format(
              "This class is not run by '%s'(%s)", ScriptiveSuiteSet.class.getCanonicalName(), runnerClass.getCanonicalName()
          ));
        }
        throw new ScriptiveUnitException(format(
            "This class (%s) is not annotated with '%s'", suiteSetClass.getCanonicalName(), RunWith.class.getCanonicalName()
        ));
      }
    },
    RUNNER {
      @Override
      public Documentation create(Class<?> driverClass) {
        return new Documentation() {
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

    private static List<String> getDocFromSpecifiedClass(String name) {
      try {
        return asList(forName(name).getAnnotation(Doc.class).value());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(format("Suite set '%s' was not found in your class path.", name), e);
      }
    }

    public abstract Documentation create(Class<?> driverClass);

    private static List<String> getRunnerClasses(Class<? extends Runner> runnerClass) {
      return Utils.allTypesAnnotatedWith("", RunWith.class)
          .filter(aClass -> aClass.getAnnotation(RunWith.class).value().equals(runnerClass))
          .map(Class::getCanonicalName)
          .collect(toList());
    }

    private static Description createDescriptionFromSpecifiedClass(String name) {
      return new Description() {
        @Override
        public String name() {
          return name;
        }

        @Override
        public List<String> content() {
          return getDocFromSpecifiedClass(name);
        }
      };
    }
  }

}
