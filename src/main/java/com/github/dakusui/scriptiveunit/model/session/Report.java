package com.github.dakusui.scriptiveunit.model.session;

import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitUnclassifiedException;
import com.github.dakusui.scriptiveunit.model.desc.testitem.IndexedTestCase;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.utils.Checks;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapMinimally;
import static java.lang.String.format;

public interface Report extends Map<String, Object> {
  void submit();

  static Report create(File baseDir, final File applicationDirectory, final String testSuiteName, final String reportFileName, IndexedTestCase testCase, TestOracle testOracle) {
    int testCaseId = testCase.getIndex();
    int oracleId = testOracle.getIndex();
    return new Base() {
      private final File base = baseDir;

      @Override
      public void submit() {
        File reportFile = new File(ensureDirectoryExists(reportingDirectory()), reportFileName);
        try {
          new ObjectMapper().writeValue(reportFile, this);
        } catch (IOException e) {
          throw wrapMinimally(format("Failed to write to a report file '%s'", reportFile), e);
        }
      }

      private File reportingDirectory() {
        return base == null ?
            reportingDirectory_() :
            new File(base, reportingDirectory_().getPath());
      }

      private File reportingDirectory_() {
        return new File(
            new File("reports"),
            new File(testSuiteName,
                new File(
                    this.applicationDirectory(),
                    new File(
                        Integer.toString(oracleId),
                        Integer.toString(testCaseId)
                    ).getPath()
                ).getPath()
            ).getPath()
        );
      }

      private File applicationDirectory() {
        return applicationDirectory;
      }

      private File ensureDirectoryExists(File dir) {
        if (!dir.exists()) {
          Checks.check(dir.mkdirs(), () -> new ScriptiveUnitUnclassifiedException(format("Failed to create a directory '%s'.", dir)));
          return dir;
        }
        return Checks.check(dir, File::isDirectory, () -> new ScriptiveUnitUnclassifiedException(format("'%s' exists, but not a directory.", dir)));
      }
    };
  }

  abstract class Base extends TreeMap<String, Object> implements Report {
  }
}
