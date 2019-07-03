package com.github.dakusui.scriptiveunit.testutils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.core.Config;
import com.github.dakusui.scriptiveunit.featuretests.AssertionMessageTest;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.session.Report;
import com.github.dakusui.scriptiveunit.model.session.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public enum UtUtils {
  ;

  public static Stage createOracleLevelStage() {
    TestItem testItem = createTestItem();
    String testSuiteName = "(noname)";
    File baseDir = new File(".");
    String reportFileName = "report.bin";
    return Stage.Factory.oracleLevelStageFor(
        config(),
        testItem,
        createResponse(),
        createThrowable(),
        Report.create(testItem, testSuiteName, baseDir, reportFileName)
    );
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
    return null;
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
        return null;
      }
    };
  }

  private static TestCase createTestCase() {
    return new TestCase() {
      @Override
      public Tuple get() {
        return null;
      }

      @Override
      public Category getCategory() {
        return null;
      }

      @Override
      public List<Constraint> violatedConstraints() {
        return null;
      }
    };
  }

  static Config config() {
    return new Config.Builder(DummyDriver.class, new Properties())
        .withScriptResourceName("")
        .build();
  }

  @Load(with = DummyDriver.Loader.class)
  public static class DummyDriver extends AssertionMessageTest.Simple {
  }
}
