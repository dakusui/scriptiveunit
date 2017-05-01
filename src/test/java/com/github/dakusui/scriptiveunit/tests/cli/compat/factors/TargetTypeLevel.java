package com.github.dakusui.scriptiveunit.tests.cli.compat.factors;

import com.github.dakusui.scriptiveunit.tests.cli.SimpleDriver;
import com.github.dakusui.scriptiveunit.tests.cli.SuiteSetDriver;

public enum TargetTypeLevel {
  FUNCTION("print") {
    @Override
    protected String invalid(CliTest cliTest) {
      return "////\\\\";
    }

    @Override
    protected String validNotFound(CliTest cliTest) {
      return "notExistingFunction";
    }
  },
  TESTCLASS(SimpleDriver.class.getCanonicalName()) {
    @Override
    protected String invalid(CliTest cliTest) {
      return "////\\\\";
    }

    @Override
    protected String validNotFound(CliTest cliTest) {
      return "not.existing.TestClass";
    }
  },
  SUITESET(SuiteSetDriver.class.getCanonicalName()) {
    @Override
    protected String invalid(CliTest cliTest) {
      return null;
    }

    @Override
    protected String validNotFound(CliTest cliTest) {
      return null;
    }
  },
  SCRIPT("tests/cli/script1.json", "test/cli/script2.json") {
    @Override
    protected String invalid(CliTest cliTest) {
      return null;
    }

    @Override
    protected String validNotFound(CliTest cliTest) {
      return "not/found/script.json";
    }
  },
  RUNNER(
      "groupByTestOracle",
      "groupByTestCase",
      "groupByTestFixture",
      "groupByTestFixtureOrderByTestOracle") {
    @Override
    protected String invalid(CliTest cliTest) {
      return "invalidRunner";
    }

    @Override
    protected String validNotFound(CliTest cliTest) {
      return "undefinedRunner";
    }
  },
  INVALID() {
    @Override
    protected String invalid(CliTest cliTest) {
      return null;
    }

    @Override
    protected String validNotFound(CliTest cliTest) {
      return null;
    }
  };

  final private String[] validLevelsForTarget;

  TargetTypeLevel(String... validLevelsForTarget) {
    this.validLevelsForTarget = validLevelsForTarget;
  }

  public String[] getValidLevelsForTarget() {
    return validLevelsForTarget;
  }

  public int numValidLevels() {
    return this.getValidLevelsForTarget().length;
  }

  public String getTargetName(CliTest cliTest) {
    return valid(cliTest.targetNameIndex);
  }

  protected String valid(int index) {
    return this.getValidLevelsForTarget()[index];
  }

  protected abstract String invalid(CliTest cliTest);

  protected abstract String validNotFound(CliTest cliTest);

}
