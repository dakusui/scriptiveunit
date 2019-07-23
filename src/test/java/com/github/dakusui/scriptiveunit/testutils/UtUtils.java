package com.github.dakusui.scriptiveunit.testutils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.scriptiveunit.annotations.CompileWith;
import com.github.dakusui.scriptiveunit.annotations.RunScript;
import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.featuretests.AssertionMessageTest;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.form.FormRegistry;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;
import com.github.dakusui.scriptiveunit.model.session.Report;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public enum UtUtils {
  ;
  private static final File BASE_DIR = new File(
      "target/report_base",
      new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss.SSSZ").format(new Date()));

  public static <T> Value<T> createForm(T value) {
    return s -> value;
  }

  public static Stage createOracleStage() {
    TestItem testItem = createTestItem();
    String testSuiteName = "_noname_";
    File applicationDir = new File("unittest");
    applicationDir.deleteOnExit();
    String reportFileName = "report.bin";
    return Stage.Factory.oracleStageFor(
        createScript(),
        createResponse(),
        testItem.getTestCase(), testItem.getTestOracle(), Report.create(BASE_DIR, applicationDir, testSuiteName, reportFileName, testItem.getTestCase(), testItem.getTestOracle()), createThrowable()
    );
  }

  public static Stage createFrameworkStage() {
    return Stage.Factory.frameworkStageFor(createScript(), new Tuple.Impl()
    );
  }

  static JsonScript createScript() {
    return new JsonScript.FromDriverClass(DummyDriver.class, "(none)");
  }

  private static TestItem createTestItem() {
    return TestItem.create(
        new IndexedTestCase(0, createTestCase()),
        createTestOracle(createEmptyFormRegistry()));
  }

  private static Throwable createThrowable() {
    return new Exception();
  }

  private static Object createResponse() {
    return "RESPONSE_STRING";
  }

  private static FormRegistry createEmptyFormRegistry() {
    return FormRegistry.createFormRegistry(new Object());
  }

  private static TestOracle createTestOracle(FormRegistry formRegistry) {
    return new TestOracle.Impl(
        0,
        "dummy",
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        Statement.Factory.create(formRegistry, Collections.emptyMap())
    );
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

  public static <V> Value.Const<V> value(V value) {
    return Value.Const.createConst(value);
  }

  @SuppressWarnings("unchecked")
  public static <V> ValueList<V> values(V... values) {
    return ValueList.create(Arrays.stream(values).map(UtUtils::value).collect(toList()));
  }

  @RunScript(compiler = @CompileWith(AssertionMessageTest.Simple.Compiler.class))
  public static class DummyDriver extends AssertionMessageTest.Simple {
  }

  public static void main(String... args) {
    System.out.println(new File(new File("target"), "dirname").toPath());
  }

  public interface TestItem {
    IndexedTestCase getTestCase();

    TestOracle getTestOracle();

    class Impl implements TestItem {
      private final IndexedTestCase indexedTestCase;
      private final TestOracle      testOracle;

      Impl(IndexedTestCase indexedTestCase, TestOracle testOracle) {
        this.indexedTestCase = indexedTestCase;
        this.testOracle = testOracle;
      }

      @Override
      public IndexedTestCase getTestCase() {
        return this.indexedTestCase;
      }

      @Override
      public TestOracle getTestOracle() {
        return this.testOracle;
      }
    }

    static TestItem create(IndexedTestCase testCase, TestOracle testOracle) {
      return new Impl(testCase, testOracle);
    }
  }
}
