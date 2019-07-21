package com.github.dakusui.scriptiveunit.testutils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.featuretests.AssertionMessageTest;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.session.Report;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static java.util.Collections.emptyList;

public enum UtUtils {
  ;
  private static final File BASE_DIR = new File(
      "target/report_base",
      new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss.SSSZ"	).format(new Date()));

  public static <T> Value<T> createForm(T value) {
    return s -> value;
  }

  public static Stage createOracleLevelStage() {
    TestItem testItem = createTestItem();
    String testSuiteName = "_noname_";
    File applicationDir = new File("unittest");
    applicationDir.deleteOnExit();
    String reportFileName = "report.bin";
    return Stage.Factory.oracleLevelStageFor(
        config(),
        testItem,
        createResponse(),
        createThrowable(),
        Report.create(BASE_DIR, applicationDir, testSuiteName, testItem, reportFileName)
    );
  }

  static JsonScript config() {
    return new JsonScript.Compat(
        DummyDriver.class,
        new Properties(),
        "(none)");
  }

  private static File createTemporaryDirectory() {
    try {
      return Files.createTempDirectory("target").toFile();
    } catch (IOException e) {
      throw ScriptiveUnitException.wrapIfNecessary(e);
    }
  }

  private static TestItem createTestItem() {
    return TestItem.create(
        new IndexedTestCase(0, createTestCase()),
        createTestOracle());
  }

  private static Throwable createThrowable() {
    return new Exception();
  }

  private static Object createResponse() {
    return "RESPONSE_STRING";
  }

  private static TestOracle createTestOracle() {
    return new TestOracle() {
      @Override
      public int getIndex() {
        return 0;
      }

      @Override
      public Optional<String> getDescription() {
        return Optional.empty();
      }

      @Override
      public Definition definition() {
        return new Definition() {
          @Override
          public Optional<Statement> before() {
            return Optional.empty();
          }

          @Override
          public Optional<Statement> given() {
            return Optional.empty();
          }

          @Override
          public Statement when() {
            return createEmptyStatement();
          }

          @Override
          public Statement then() {
            return createEmptyStatement();
          }

          @Override
          public Optional<Statement> onFailure() {
            return Optional.empty();
          }

          @Override
          public Optional<Statement> after() {
            return Optional.empty();
          }
        };
      }
    };
  }

  private static Statement createEmptyStatement() {
    return new Statement() {
      @Override
      public <U> Value<U> toValue() {
        return input -> {
          throw new UnsupportedOperationException();
        };
      }
    };
  }

  private static TestCase createTestCase() {
    return new TestCase() {
      @Override
      public Tuple get() {
        return new Tuple.Impl();
      }

      @Override
      public Category getCategory() {
        return Category.REGULAR;
      }

      @Override
      public List<Constraint> violatedConstraints() {
        return emptyList();
      }
    };
  }

  @RunScript(compileWith = AssertionMessageTest.Simple.Compiler.class)
  public static class DummyDriver extends AssertionMessageTest.Simple {
  }

  public static void main(String... args) {
    System.out.println(new File(new File("target"), "dirname").toPath());
  }
}
